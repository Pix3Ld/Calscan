package com.example.caloriecounter.activity

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.database.sqlite.SQLiteException
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.caloriecounter.MainActivity
import com.example.caloriecounter.R
import com.example.caloriecounter.data.ActivitySessionEntity
import com.example.caloriecounter.data.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * Długotrwały serwis pomiaru GPS i kroków, utrzymujący sesję także przy wygaszonym ekranie.
 */
class ActivityTrackingService : Service(), SensorEventListener, LocationListener {
    companion object {
        const val TYPE_WALK = "WALK"
        const val TYPE_RUN = "RUN"
        private const val ACTION_START = "com.example.caloriecounter.activity.START"
        private const val ACTION_STOP = "com.example.caloriecounter.activity.STOP"
        private const val EXTRA_TYPE = "activity_type"
        private const val EXTRA_WEIGHT = "weight_kg"
        private const val NOTIFICATION_ID = 4102
        private const val NOTIFICATION_CHANNEL = "activity_tracking"

        /** Uruchamia serwis foreground z wybranym rodzajem aktywności i masą ciała. */
        fun start(context: Context, activityType: String, weightKg: Float) {
            val intent = Intent(context, ActivityTrackingService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_TYPE, activityType)
                .putExtra(EXTRA_WEIGHT, weightKg)
            ContextCompat.startForegroundService(context, intent)
        }

        /** Kończy bieżącą sesję i zapisuje jej podsumowanie do bazy. */
        fun stop(context: Context) {
            context.startService(
                Intent(context, ActivityTrackingService::class.java).setAction(ACTION_STOP)
            )
        }
    }

    // Operacje Room wykonują się poza wątkiem lokalizacji i czujników.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var startedAt = 0L
    private var activityType = TYPE_WALK
    private var weightKg = 70f
    private var steps = 0
    private var previousSensorCount: Float? = null
    private var stepsAtLastGps = 0
    private var previousLocation: Location? = null
    private var distanceMeters = 0.0
    private var breakPending = false
    private val routeSegments = mutableListOf<MutableList<TrackCoordinate>>()
    private val mainHandler = android.os.Handler(Looper.getMainLooper())
    // Publikuje czas sesji co sekundę, również gdy nie przyszła nowa lokalizacja.
    private val ticker = object : Runnable {
        override fun run() {
            publishSnapshot()
            if (startedAt != 0L) mainHandler.postDelayed(this, 1_000L)
        }
    }

    /** Inicjalizuje menedżery Androida i kanał powiadomienia foreground. */
    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        createNotificationChannel()
    }

    /** Rozdziela komendy rozpoczęcia i zakończenia sesji. */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> beginTracking(intent, startId)
            ACTION_STOP -> finishTracking()
            else -> stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    // Sprawdza wymagania GPS, zeruje stan i rejestruje nasłuch lokalizacji oraz kroków.
    private fun beginTracking(intent: Intent, startId: Int) {
        if (startedAt != 0L) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityTrackingState.reportError(getString(R.string.activity_location_permission))
            stopSelf(startId)
            return
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ActivityTrackingState.reportError(getString(R.string.activity_location_error))
            stopSelf(startId)
            return
        }

        activityType = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_WALK
        weightKg = intent.getFloatExtra(EXTRA_WEIGHT, 70f)
        startedAt = System.currentTimeMillis()
        steps = 0
        previousSensorCount = null
        stepsAtLastGps = 0
        previousLocation = null
        distanceMeters = 0.0
        breakPending = false
        routeSegments.clear()

        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            stepSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1_000L,
                0f,
                this,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            ActivityTrackingState.reportError(getString(R.string.activity_location_permission))
            stopForeground(STOP_FOREGROUND_REMOVE)
            startedAt = 0L
            stopSelf(startId)
            return
        } catch (exception: IllegalArgumentException) {
            ActivityTrackingState.reportError(getString(R.string.activity_location_error))
            stopForeground(STOP_FOREGROUND_REMOVE)
            startedAt = 0L
            stopSelf(startId)
            return
        }
        ActivityTrackingState.publish(
            ActivitySnapshot(
                isTracking = true,
                activityType = activityType,
                routeSegments = emptyList()
            )
        )
        mainHandler.post(ticker)
    }

    // Zatrzymuje czujniki, publikuje wynik końcowy i zapisuje trasę wraz z metrykami.
    private fun finishTracking() {
        if (startedAt == 0L) {
            stopSelf()
            return
        }
        val finalSnapshot = createSnapshot()
        startedAt = 0L
        mainHandler.removeCallbacks(ticker)
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        ActivityTrackingState.publish(finalSnapshot.copy(isTracking = false))
        stopForeground(STOP_FOREGROUND_REMOVE)
        val routeJson = JSONArray().apply {
            finalSnapshot.routeSegments.forEach { segment ->
                put(JSONArray().apply {
                    segment.forEach { point ->
                        put(JSONArray().put(point.latitude).put(point.longitude))
                    }
                })
            }
        }.toString()

        serviceScope.launch {
            try {
                DatabaseProvider.get(applicationContext).mealDao().insertActivitySession(
                    ActivitySessionEntity(
                        activityType = activityType,
                        durationSeconds = finalSnapshot.elapsedSeconds,
                        distanceMeters = finalSnapshot.distanceMeters,
                        steps = finalSnapshot.steps,
                        caloriesKcal = finalSnapshot.caloriesKcal,
                        routeJson = routeJson
                    )
                )
            } catch (exception: SQLiteException) {
                ActivityTrackingState.reportError(getString(R.string.activity_save_error))
            } finally {
                stopSelf()
            }
        }
    }

    // Sensor typu STEP_COUNTER podaje wartość narastającą od startu urządzenia.
    override fun onSensorChanged(event: SensorEvent?) {
        val totalCount = event?.values?.firstOrNull() ?: return
        val previousCount = previousSensorCount
        if (previousCount != null) {
            steps += (totalCount - previousCount).toInt().coerceAtLeast(0)
        }
        previousSensorCount = totalCount
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    // Dolicza odcinek tylko wtedy, gdy GPS, kroki i wyliczona prędkość są wiarygodne.
    override fun onLocationChanged(location: Location) {
        if (startedAt == 0L) return
        val previous = previousLocation
        if (previous != null) {
            val distance = previous.distanceTo(location)
            val interval = (location.time - previous.time) / 1_000f
            val stepDelta = steps - stepsAtLastGps
            if (ActivityMetrics.shouldCountSegment(
                    distance,
                    interval,
                    maxOf(location.accuracy, previous.accuracy),
                    stepDelta
                )
            ) {
                if (routeSegments.isEmpty() || breakPending) {
                    routeSegments.add(mutableListOf())
                    routeSegments.last().add(previous.toCoordinate())
                }
                routeSegments.last().add(location.toCoordinate())
                distanceMeters += distance
                breakPending = false
            } else if (distance > 15f) {
                breakPending = true
            }
        }
        previousLocation = location
        stepsAtLastGps = steps
        publishSnapshot()
    }

    @Deprecated("Deprecated in Android")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

    override fun onProviderEnabled(provider: String) = Unit

    // Kończy pomiar, jeśli użytkownik wyłączy GPS w trakcie sesji.
    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER && startedAt != 0L) {
            finishTracking()
            ActivityTrackingState.reportError(getString(R.string.activity_location_error))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /** Usuwa callbacki oraz rejestracje czujników, niezależnie od sposobu zamknięcia serwisu. */
    override fun onDestroy() {
        mainHandler.removeCallbacks(ticker)
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
        serviceScope.cancel()
        super.onDestroy()
    }

    // Tworzy spójną migawkę z aktualnego czasu, kroków, dystansu i trasy.
    private fun createSnapshot(): ActivitySnapshot {
        val elapsed = if (startedAt == 0L) 0L else
            ((System.currentTimeMillis() - startedAt) / 1_000L).coerceAtLeast(0)
        return ActivitySnapshot(
            isTracking = startedAt != 0L,
            activityType = activityType,
            elapsedSeconds = elapsed,
            distanceMeters = distanceMeters,
            steps = steps,
            caloriesKcal = ActivityMetrics.estimateCalories(activityType, weightKg, elapsed),
            routeSegments = routeSegments.map { it.toList() }
        )
    }

    // Aktualizuje obserwowany stan aplikacji i treść stałego powiadomienia.
    private fun publishSnapshot() {
        if (startedAt == 0L) return
        val snapshot = createSnapshot()
        ActivityTrackingState.publish(snapshot)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(snapshot))
    }

    // Buduje stałe powiadomienie wymagane przez foreground service lokalizacji.
    private fun buildNotification(snapshot: ActivitySnapshot = createSnapshot()): Notification =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(getString(R.string.activity_tracking_notification))
            .setContentText(
                getString(
                    R.string.activity_notification_progress,
                    snapshot.distanceMeters / 1_000.0,
                    snapshot.steps
                )
            )
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    // Kanał o niskim priorytecie nie odrywa użytkownika od innych zadań.
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                getString(R.string.activity_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    // Przechowuje wyłącznie współrzędne potrzebne do narysowania historii trasy.
    private fun Location.toCoordinate() = TrackCoordinate(latitude, longitude)
}
