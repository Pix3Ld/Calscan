package com.example.caloriecounter.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.BuildConfig
import com.example.caloriecounter.R
import com.example.caloriecounter.data.ActivitySessionEntity
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
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
    private val bodyWeightKg: () -> Float,
    private val onStart: (String, Float) -> Unit,
    private val onStop: () -> Unit,
    private val onSessionClick: (ActivitySessionEntity) -> Unit,
    private val onDeleteSession: (Long) -> Unit
) {
    private var mapView: MapView? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var stepsView: TextView? = null
    private var distanceView: TextView? = null
    private var timeView: TextView? = null
    private var caloriesView: TextView? = null
    private var statusView: TextView? = null
    private var startButton: MaterialButton? = null
    private var stopButton: MaterialButton? = null
    private var activityTypeTabs: List<View> = emptyList()
    private var activityTypeLabels: List<TextView> = emptyList()
    private var activityTypeIndicators: List<View> = emptyList()
    private var selectedActivityType = ActivityTrackingService.TYPE_WALK
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

        content.addView(buildTypeSelector())
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
        val headerRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12.dp() }
        }

        val backBtn = styledButton(activity.getString(R.string.details_back), false).apply {
            setIconResource(R.drawable.ic_arrow_back_24)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconPadding = 8.dp()
            setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
        }

        val deleteBtn = android.widget.ImageButton(activity).apply {
            setImageResource(R.drawable.ic_delete_24)
            contentDescription = activity.getString(R.string.activity_delete)
            background = android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
            setColorFilter(if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586"))
            setOnClickListener {
                onDeleteSession(session.id)
                activity.onBackPressedDispatcher.onBackPressed()
            }
        }

        headerRow.addView(backBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            42.dp()
        ))
        headerRow.addView(View(activity), LinearLayout.LayoutParams(0, 0, 1f))
        headerRow.addView(deleteBtn, LinearLayout.LayoutParams(44.dp(), 44.dp()))
        content.addView(headerRow)

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
        ).apply { setPadding(0, 0, 0, 8.dp()) })

        val route = decodeRoute(session.routeJson)
        if (route.isEmpty()) {
            content.addView(pageText(activity.getString(R.string.activity_route_empty), 14f, false).apply {
                setPadding(0, 8.dp(), 0, 8.dp())
            })
        } else {
            content.addView(buildMap(heightDp = 380))
            drawRoute(route, centerOnFirstPoint = false)
            fitMapToRoute(route)
        }

        val telemetryCard = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14.dp(), 12.dp(), 14.dp(), 12.dp())
            background = GradientDrawable().apply {
                cornerRadius = 12.dp().toFloat()
                setColor(if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE)
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12.dp() }
        }
        val tRow1 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val stepsTv = pageText(activity.getString(R.string.activity_steps_template, session.steps), 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        val distTv = pageText(String.format(Locale.getDefault(), "%.2f km", session.distanceMeters / 1_000.0), 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        tRow1.addView(stepsTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        tRow1.addView(distTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val tRow2 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 6.dp() }
        }
        val timeTv = pageText(formatDuration(session.durationSeconds), 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        val calTv = pageText(String.format(Locale.getDefault(), "%d kcal", session.caloriesKcal), 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        tRow2.addView(timeTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        tRow2.addView(calTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        telemetryCard.addView(tRow1)
        telemetryCard.addView(tRow2)
        content.addView(telemetryCard)
    }

    /** Odświeża metryki, przyciski i trasę bez przebudowy całej strony. */
    fun update(snapshot: ActivitySnapshot, sessions: List<ActivitySessionEntity>) {
        lastSnapshot = snapshot
        stepsView?.text = activity.getString(R.string.activity_steps_template, snapshot.steps)
        distanceView?.text = String.format(Locale.getDefault(), "%.1f km", snapshot.distanceMeters / 1_000.0)
        timeView?.text = formatDuration(snapshot.elapsedSeconds)
        caloriesView?.text = String.format(Locale.getDefault(), "%d kcal", snapshot.caloriesKcal)
        startButton?.visibility = if (snapshot.isTracking) View.GONE else View.VISIBLE
        stopButton?.visibility = if (snapshot.isTracking) View.VISIBLE else View.GONE
        updateActivityTypeTabs()
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
    private fun buildTypeSelector(): View {
        selectedActivityType = lastSnapshot.activityType
        val selector = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 4.dp(), 0, 8.dp())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val types = listOf(
            ActivityTrackingService.TYPE_WALK to R.string.activity_type_walk,
            ActivityTrackingService.TYPE_RUN to R.string.activity_type_run
        )
        val tabViews = mutableListOf<View>()
        val labels = mutableListOf<TextView>()
        val indicators = mutableListOf<View>()

        types.forEach { (type, labelRes) ->
            val label = pageText(activity.getString(labelRes), 16f, false).apply {
                gravity = android.view.Gravity.CENTER
            }
            val indicator = View(activity)
            val tab = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                isClickable = true
                isFocusable = true
                addView(label, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ))
                addView(indicator, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    2.dp()
                ).apply { topMargin = 8.dp() })
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    if (!lastSnapshot.isTracking) {
                        selectedActivityType = type
                        updateActivityTypeTabs()
                    }
                }
            }
            selector.addView(tab)
            tabViews.add(tab)
            labels.add(label)
            indicators.add(indicator)
        }

        activityTypeTabs = tabViews
        activityTypeLabels = labels
        activityTypeIndicators = indicators
        updateActivityTypeTabs()
        return selector
    }

    private fun updateActivityTypeTabs() {
        val types = listOf(ActivityTrackingService.TYPE_WALK, ActivityTrackingService.TYPE_RUN)
        types.forEachIndexed { index, type ->
            val selected = selectedActivityType == type
            activityTypeTabs.getOrNull(index)?.apply {
                isEnabled = !lastSnapshot.isTracking
                alpha = if (lastSnapshot.isTracking) 0.65f else 1f
            }
            activityTypeLabels.getOrNull(index)?.apply {
                setTextColor(
                    if (selected) {
                        if (isDarkTheme()) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C")
                    } else {
                        if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586")
                    }
                )
                setTypeface(typeface, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
            activityTypeIndicators.getOrNull(index)?.setBackgroundColor(
                if (selected) {
                    if (isDarkTheme()) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C")
                } else {
                    Color.TRANSPARENT
                }
            )
        }
    }

    // Dwa wiersze pokazują kroki, dystans, czas i szacowane kalorie w dwoch wyrównanych kolumnach.
    private fun buildMetrics(): View {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14.dp(), 12.dp(), 14.dp(), 12.dp())
            background = GradientDrawable().apply {
                cornerRadius = 12.dp().toFloat()
                setColor(if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE)
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8.dp() }
        }

        val row1 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val stepsTv = pageText("", 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        val distanceTv = pageText("", 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        row1.addView(stepsTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row1.addView(distanceTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val row2 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 6.dp() }
        }
        val timeTv = pageText("", 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        val caloriesTv = pageText("", 16f, true).apply {
            gravity = android.view.Gravity.CENTER
        }
        row2.addView(timeTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row2.addView(caloriesTv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        container.addView(row1)
        container.addView(row2)

        stepsView = stepsTv
        distanceView = distanceTv
        timeView = timeTv
        caloriesView = caloriesTv

        return container
    }

    // Konfiguruje mapę OpenStreetMap, która wyświetla bieżącą pozycję i przebytą trasę.
    private fun buildMap(heightDp: Int = 220): FrameLayout {
        val configuration = Configuration.getInstance()
        configuration.userAgentValue =
            "${activity.getString(R.string.app_name)}/${BuildConfig.VERSION_NAME} " +
                "(+https://github.com/Pix3Ld/calvic-a-callorie-app)"
        configuration.getOsmdroidTileCache(activity)
        val map = MapView(activity).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(5.0)
            controller.setCenter(GeoPoint(52.2297, 21.0122))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mapView = this
            onResume()
        }
        return FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightDp.dp()
            ).apply { topMargin = 8.dp() }
            addView(map)
            addView(TextView(activity).apply {
                text = activity.getString(R.string.map_attribution)
                textSize = 10f
                setTextColor(if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
                setPadding(6.dp(), 3.dp(), 6.dp(), 3.dp())
                background = GradientDrawable().apply {
                    cornerRadius = 4.dp().toFloat()
                    setColor(
                        if (isDarkTheme()) Color.argb(220, 31, 41, 55)
                        else Color.argb(220, 255, 255, 255)
                    )
                }
                isClickable = false
                isFocusable = false
            }, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.BOTTOM or android.view.Gravity.START
            ).apply {
                marginStart = 6.dp()
                bottomMargin = 6.dp()
            })
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
            setTextColor(if (isDarkTheme()) Color.parseColor("#FCD34D") else Color.parseColor("#B45309"))
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
                val weight = bodyWeightKg()
                if (weight !in 20f..300f) {
                    statusView?.text = activity.getString(R.string.profile_weight_invalid)
                    statusView?.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                onStart(selectedActivityType, weight)
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
            val cardRow = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(12.dp(), 10.dp(), 8.dp(), 10.dp())
                background = GradientDrawable().apply {
                    cornerRadius = 12.dp().toFloat()
                    setColor(if (isDarkTheme()) Color.rgb(31, 41, 55) else Color.WHITE)
                }
            }

            val cardDetails = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                isClickable = true
                isFocusable = true
                setOnClickListener { onSessionClick(session) }
            }
            cardDetails.contentDescription = activity.getString(R.string.activity_history_open)
            cardDetails.addView(pageText(
                activity.getString(
                    R.string.activity_history_date,
                    type,
                    date
                ),
                15f,
                true
            ))
            cardDetails.addView(pageText(
                activity.getString(
                    R.string.activity_session_template,
                    formatDuration(session.durationSeconds),
                    session.distanceMeters / 1_000.0,
                    session.caloriesKcal
                ),
                14f,
                false
            ))
            val deleteBtn = android.widget.ImageButton(activity).apply {
                setImageResource(R.drawable.ic_delete_24)
                contentDescription = activity.getString(R.string.activity_delete)
                background = android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
                setColorFilter(if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586"))
                setOnClickListener { onDeleteSession(session.id) }
            }

            cardRow.addView(cardDetails)
            cardRow.addView(deleteBtn, LinearLayout.LayoutParams(44.dp(), 44.dp()))
            container.addView(cardRow, LinearLayout.LayoutParams(
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
