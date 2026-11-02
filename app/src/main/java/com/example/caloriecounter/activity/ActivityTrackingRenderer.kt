package com.example.caloriecounter.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.BuildConfig
import com.example.caloriecounter.R
import com.example.caloriecounter.data.ActivitySessionEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.appcompat.content.res.AppCompatResources
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Buduje ekran rejestrowania marszu/biegu, mapę trasy i listę zapisanych sesji.
 */
class ActivityTrackingRenderer(
    private val activity: AppCompatActivity,
    private val pageContainer: LinearLayout,
    private val pageText: (String, Float, Boolean) -> TextView,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val isDarkTheme: () -> Boolean,
    private val onStart: (String, Float) -> Unit,
    private val onStop: () -> Unit,
    private val onSessionClick: (ActivitySessionEntity) -> Unit
) {
    private var mapView: MapView? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var metricsView: TextView? = null
    private var statusView: TextView? = null
    private var startButton: MaterialButton? = null
    private var stopButton: MaterialButton? = null
    private var weightInput: EditText? = null
    private var activityTypeGroup: RadioGroup? = null
    private var historyContainer: LinearLayout? = null
    private var lastSnapshot = ActivitySnapshot()
    private var currentSessions: List<ActivitySessionEntity>? = null
    private val routeOverlays = mutableListOf<Polyline>()
    private var trackingMapCentered = false

    /** Odtwarza widoki ekranu i mapę, a następnie pokazuje aktualny stan oraz historię. */
    fun render(snapshot: ActivitySnapshot, sessions: List<ActivitySessionEntity>) {
        releaseMap()
        pageContainer.removeAllViews()
        trackingMapCentered = false

        val scrollView = ScrollView(activity)
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(content)
        pageContainer.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        content.addView(pageText(activity.getString(R.string.activity_intro), 15f, false))
        content.addView(buildTypeSelector())
        content.addView(buildWeightInput())
        content.addView(buildMetrics())
        content.addView(buildMap())
        content.addView(buildStatus())
        content.addView(buildControls())
        content.addView(pageText(activity.getString(R.string.activity_history), 19f, true).apply {
            setPadding(0, 20.dp(), 0, 8.dp())
        })
        historyContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(historyContainer)
        currentSessions = null

        update(snapshot, sessions)
    }

    /** Pokazuje zapisany spacer lub bieg na mapie dopasowanej do całego śladu GPS. */
    fun renderSessionDetails(container: LinearLayout, session: ActivitySessionEntity) {
        releaseMap()
        container.removeAllViews()
        trackingMapCentered = false

        val scrollView = ScrollView(activity)
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(content)
        container.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        content.addView(styledButton(activity.getString(R.string.details_back), false).apply {
            setIconResource(R.drawable.ic_arrow_back_24)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconPadding = 8.dp()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                48.dp()
            ).apply { bottomMargin = 12.dp() }
            setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
        })

        val type = if (session.activityType == ActivityTrackingService.TYPE_RUN) {
            activity.getString(R.string.activity_type_run_short)
        } else {
            activity.getString(R.string.activity_type_walk_short)
        }
        val date = SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault())
            .format(Date(session.createdAt))
        content.addView(pageText(
            activity.getString(R.string.activity_history_date, type, date),
            20f,
            true
        ))
        content.addView(pageText(
            activity.getString(
                R.string.activity_session_template,
                formatDuration(session.durationSeconds),
                session.distanceMeters / 1_000.0,
                session.caloriesKcal
            ),
            16f,
            false
        ).apply { setPadding(0, 8.dp(), 0, 4.dp()) })
        content.addView(pageText(
            activity.getString(R.string.activity_steps_template, session.steps),
            15f,
            false
        ).apply { setPadding(0, 0, 0, 12.dp()) })

        val route = decodeRoute(session.routeJson)
        if (route.isEmpty()) {
            content.addView(pageText(activity.getString(R.string.activity_route_empty), 14f, false))
        } else {
            content.addView(buildMap(heightDp = 380))
            drawRoute(route, centerOnFirstPoint = false)
            fitMapToRoute(route)
        }
    }

    /** Odświeża metryki, przyciski i trasę bez przebudowy całej strony. */
    fun update(snapshot: ActivitySnapshot, sessions: List<ActivitySessionEntity>) {
        lastSnapshot = snapshot
        metricsView?.text = activity.getString(
            R.string.activity_metrics_template,
            snapshot.steps,
            snapshot.distanceMeters / 1_000.0,
            formatDuration(snapshot.elapsedSeconds),
            snapshot.caloriesKcal
        )
        startButton?.visibility = if (snapshot.isTracking) View.GONE else View.VISIBLE
        stopButton?.visibility = if (snapshot.isTracking) View.VISIBLE else View.GONE
        activityTypeGroup?.childrenEnabled(!snapshot.isTracking)
        weightInput?.isEnabled = !snapshot.isTracking
        startButton?.isEnabled = !snapshot.isTracking

        val status = snapshot.errorMessage ?: when {
            !hasStepCounter() -> activity.getString(R.string.activity_sensor_warning)
            else -> ""
        }
        statusView?.text = status
        statusView?.visibility = if (status.isEmpty()) View.GONE else View.VISIBLE

        val route = if (snapshot.isTracking || snapshot.routeSegments.isNotEmpty()) {
            snapshot.routeSegments
        } else {
            emptyList()
        }
        drawRoute(route, centerOnFirstPoint = route.isNotEmpty() && !trackingMapCentered)
        if (currentSessions != sessions) {
            currentSessions = sessions
            renderSessions(sessions)
        }
    }

    /** Wstrzymuje mapę i GPS, gdy ekran śledzenia znika z widoku. */
    fun onPageHidden() {
        myLocationOverlay?.disableMyLocation()
        mapView?.onPause()
    }

    /** Wznawia mapę oraz nakładkę lokalizacji po ponownym otwarciu strony. */
    fun onPageShown() {
        mapView?.onResume()
        enableMyLocation()
    }

    /** Wznawia mapę zapisanego śladu bez uruchamiania nakładki bieżącej lokalizacji. */
    fun onDetailsPageShown() {
        mapView?.onResume()
    }

    /** Włącza nakładkę pozycji, gdy system przyznał dostęp do lokalizacji. */
    fun onLocationPermissionGranted() {
        enableMyLocation()
    }

    /** Zwalnia zasoby mapy i lokalizacji wraz z zakończeniem aktywności. */
    fun onDestroy() {
        releaseMap()
    }

    // Pozwala wybrać marsz lub bieg; wybór blokuje się podczas aktywnego pomiaru.
    private fun buildTypeSelector(): RadioGroup {
        val radioGroup = RadioGroup(activity).apply {
            orientation = RadioGroup.HORIZONTAL
            isEnabled = !lastSnapshot.isTracking
            setPadding(0, 12.dp(), 0, 4.dp())
        }
        radioGroup.addView(RadioButton(activity).apply {
            id = View.generateViewId()
            text = activity.getString(R.string.activity_type_walk)
            isChecked = lastSnapshot.activityType == ActivityTrackingService.TYPE_WALK
            isEnabled = !lastSnapshot.isTracking
        })
        radioGroup.addView(RadioButton(activity).apply {
            id = View.generateViewId()
            text = activity.getString(R.string.activity_type_run)
            isChecked = lastSnapshot.activityType == ActivityTrackingService.TYPE_RUN
            isEnabled = !lastSnapshot.isTracking
        })
        activityTypeGroup = radioGroup
        return radioGroup
    }

    // Masa ciała jest używana przez serwis do oszacowania spalonych kalorii.
    private fun buildWeightInput(): View {
        val layout = TextInputLayout(activity).apply {
            hint = activity.getString(R.string.activity_weight_label)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }
        weightInput = EditText(activity).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("70")
            setTextColor(if (isDarkTheme()) Color.WHITE else Color.BLACK)
        }
        layout.addView(weightInput)
        return layout.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8.dp() }
        }
    }

    // Jedno pole pokazuje kroki, dystans, czas i szacowane kalorie.
    private fun buildMetrics(): TextView {
        metricsView = pageText("", 18f, true).apply {
            setPadding(0, 16.dp(), 0, 12.dp())
        }
        return metricsView!!
    }

    // Konfiguruje mapę OpenStreetMap, która wyświetla bieżącą pozycję i przebytą trasę.
    private fun buildMap(heightDp: Int = 260): MapView {
        val configuration = Configuration.getInstance()
        configuration.userAgentValue =
            "${activity.getString(R.string.app_name)}/${BuildConfig.VERSION_NAME} " +
                "(+https://github.com/Pix3Ld/calvic-a-callorie-app)"
        configuration.getOsmdroidTileCache(activity)
        return MapView(activity).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true)
            controller.setZoom(5.0)
            controller.setCenter(GeoPoint(52.2297, 21.0122))
            overlays.add(CopyrightOverlay(activity))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightDp.dp()
            ).apply { topMargin = 8.dp() }
            mapView = this
            onResume()
        }
    }

    // Dodaje marker GPS i ustawia jego strzałkę kierunku w kolorze zieleni używanym w przyciskach.
    private fun enableMyLocation() {
        val map = mapView ?: return
        val overlay = myLocationOverlay ?: MyLocationNewOverlay(
            GpsMyLocationProvider(activity),
            map
        ).also {
            it.setDirectionIcon(createDirectionArrow())
            myLocationOverlay = it
            map.overlays.add(it)
        }
        if (!overlay.isMyLocationEnabled) {
            overlay.enableMyLocation()
            overlay.runOnFirstFix {
                val location = overlay.myLocation ?: return@runOnFirstFix
                activity.runOnUiThread {
                    if (mapView === map && !trackingMapCentered) {
                        map.controller.animateTo(location)
                        map.controller.setZoom(15.0)
                        trackingMapCentered = true
                    }
                }
            }
        }
    }

    // Zamienia wektorową ikonę na bitmapę wymaganą przez nakładkę osmdroid.
    private fun createDirectionArrow(): Bitmap {
        val drawable = requireNotNull(
            AppCompatResources.getDrawable(activity, R.drawable.ic_user_direction_24)
        ) { "Missing user direction arrow drawable" }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    // Buduje miejsce na komunikaty o błędach uprawnień, GPS lub czujników.
    private fun buildStatus(): TextView {
        statusView = pageText("", 14f, false).apply {
            setTextColor(if (isDarkTheme()) Color.rgb(255, 205, 120) else Color.rgb(159, 91, 0))
            setPadding(0, 8.dp(), 0, 4.dp())
        }
        return statusView!!
    }

    // Waliduje masę ciała i przekazuje wybrany typ do kontrolera sesji.
    private fun buildControls(): View {
        val controls = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 12.dp(), 0, 4.dp())
        }
        startButton = styledButton(activity.getString(R.string.activity_start), true).apply {
            setOnClickListener {
                val weight = weightInput?.text?.toString()?.replace(',', '.')?.toFloatOrNull()
                if (weight == null || weight !in 20f..300f) {
                    statusView?.text = activity.getString(R.string.activity_weight_invalid)
                    statusView?.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                val selected = activityTypeGroup?.checkedRadioButtonId
                val selectedButton = activityTypeGroup?.findViewById<RadioButton>(selected ?: -1)
                val type = if (selectedButton?.text == activity.getString(R.string.activity_type_run)) {
                    ActivityTrackingService.TYPE_RUN
                } else {
                    ActivityTrackingService.TYPE_WALK
                }
                onStart(type, weight)
            }
        }
        stopButton = styledButton(activity.getString(R.string.activity_stop), false).apply {
            setOnClickListener { onStop() }
        }
        controls.addView(startButton, LinearLayout.LayoutParams(0, 48.dp(), 1f))
        controls.addView(stopButton, LinearLayout.LayoutParams(0, 48.dp(), 1f).apply {
            leftMargin = 8.dp()
        })
        return controls
    }

    // Renderuje historię sesji; dotknięcie karty otwiera szczegóły trasy.
    private fun renderSessions(sessions: List<ActivitySessionEntity>) {
        val container = historyContainer ?: return
        container.removeAllViews()
        if (sessions.isEmpty()) {
            container.addView(pageText(activity.getString(R.string.activity_empty), 14f, false))
            return
        }
        sessions.forEach { session ->
            val type = if (session.activityType == ActivityTrackingService.TYPE_RUN) {
                activity.getString(R.string.activity_type_run_short)
            } else {
                activity.getString(R.string.activity_type_walk_short)
            }
            val date = SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault())
                .format(Date(session.createdAt))
            val card = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12.dp(), 10.dp(), 12.dp(), 10.dp())
                setBackgroundColor(if (isDarkTheme()) Color.rgb(31, 41, 55) else Color.WHITE)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onSessionClick(session)
                }
            }
            card.contentDescription = activity.getString(R.string.activity_history_open)
            card.addView(pageText(
                activity.getString(
                    R.string.activity_history_date,
                    type,
                    date
                ),
                15f,
                true
            ))
            card.addView(pageText(
                activity.getString(
                    R.string.activity_session_template,
                    formatDuration(session.durationSeconds),
                    session.distanceMeters / 1_000.0,
                    session.caloriesKcal
                ),
                14f,
                false
            ))
            card.addView(pageText(
                activity.getString(R.string.activity_steps_template, session.steps),
                13f,
                false
            ))
            card.addView(pageText(activity.getString(R.string.activity_history_open), 13f, true).apply {
                setTextColor(Color.rgb(30, 101, 76))
                setPadding(0, 6.dp(), 0, 0)
            })
            container.addView(card, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dp() })
        }
    }

    // Zastępuje poprzednie linie na mapie nowymi, zachowując przerwy między odcinkami GPS.
    private fun drawRoute(segments: List<List<TrackCoordinate>>, centerOnFirstPoint: Boolean) {
        val map = mapView ?: return
        map.overlays.removeAll(routeOverlays.toSet())
        routeOverlays.clear()
        var firstPoint: GeoPoint? = null
        segments.forEach { segment ->
            if (segment.isEmpty()) return@forEach
            val line = Polyline(map).apply {
                setPoints(segment.map { GeoPoint(it.latitude, it.longitude) })
                outlinePaint.color = Color.rgb(30, 101, 76)
                outlinePaint.strokeWidth = 8f
            }
            firstPoint = firstPoint ?: GeoPoint(segment.first().latitude, segment.first().longitude)
            map.overlays.add(line)
            routeOverlays.add(line)
        }
        if (centerOnFirstPoint && firstPoint != null) {
            map.controller.setZoom(16.0)
            map.controller.setCenter(firstPoint)
            trackingMapCentered = true
        }
        map.invalidate()
    }

    private fun fitMapToRoute(segments: List<List<TrackCoordinate>>) {
        val map = mapView ?: return
        val points = segments.flatten().map { GeoPoint(it.latitude, it.longitude) }
        if (points.isEmpty()) return
        map.post {
            if (mapView !== map) return@post
            if (points.size == 1) {
                map.controller.setCenter(points.first())
                map.controller.setZoom(16.0)
            } else {
                map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 40.dp())
            }
            trackingMapCentered = true
        }
    }

    private fun releaseMap() {
        myLocationOverlay?.disableMyLocation()
        mapView?.onPause()
        mapView?.onDetach()
        mapView = null
        myLocationOverlay = null
        routeOverlays.clear()
    }

    // Odtwarza z bazy listę odcinków współrzędnych zapisaną jako tablice JSON.
    private fun decodeRoute(routeJson: String): List<List<TrackCoordinate>> {
        val segments = mutableListOf<List<TrackCoordinate>>()
        val jsonSegments = JSONArray(routeJson)
        for (segmentIndex in 0 until jsonSegments.length()) {
            val jsonSegment = jsonSegments.getJSONArray(segmentIndex)
            val segment = mutableListOf<TrackCoordinate>()
            for (pointIndex in 0 until jsonSegment.length()) {
                val point = jsonSegment.getJSONArray(pointIndex)
                segment.add(TrackCoordinate(point.getDouble(0), point.getDouble(1)))
            }
            if (segment.isNotEmpty()) segments.add(segment)
        }
        return segments
    }

    // Sprawdza dostępność sprzętowego licznika kroków przed pokazaniem ostrzeżenia.
    private fun hasStepCounter(): Boolean {
        val sensors = activity.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        return sensors.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) != null
    }

    private fun RadioGroup.childrenEnabled(enabled: Boolean) {
        for (index in 0 until childCount) {
            getChildAt(index).isEnabled = enabled
        }
    }

    // Formatuje czas jako MM:SS lub H:MM:SS dla dłuższych sesji.
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3_600
        val minutes = (seconds % 3_600) / 60
        val remainingSeconds = seconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
        }
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
