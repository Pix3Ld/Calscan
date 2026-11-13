package com.example.caloriecounter

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.net.Uri
import android.provider.Settings
import android.location.LocationManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.caloriecounter.camera.MealCameraController
import com.example.caloriecounter.camera.ScanPageRenderer
import com.example.caloriecounter.activity.ActivitySnapshot
import com.example.caloriecounter.activity.ActivityTrackingRenderer
import com.example.caloriecounter.activity.ActivityTrackingService
import com.example.caloriecounter.activity.ActivityTrackingState
import androidx.lifecycle.lifecycleScope
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.caloriecounter.data.DatabaseProvider
import com.example.caloriecounter.data.ActivitySessionEntity
import com.example.caloriecounter.data.MealRepository
import com.example.caloriecounter.data.MealSummaryEntity
import com.example.caloriecounter.data.PhotoHistoryEntity
import com.example.caloriecounter.history.MealHistoryRenderer
import com.example.caloriecounter.history.MealPhotoWorkflow
import com.example.caloriecounter.history.PhotoDetailsRenderer
import com.example.caloriecounter.reminders.MealReminderController
import com.example.caloriecounter.reminders.MealReminderSettingsStore
import com.example.caloriecounter.settings.DailyLimitSettingsController
import com.example.caloriecounter.settings.AppSettingsStore
import com.example.caloriecounter.settings.SettingsPageRenderer
import com.example.caloriecounter.ui.AppThemeApplier
import com.example.caloriecounter.ui.ActivityViewFactory
import com.example.caloriecounter.ui.DashboardRenderer
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var dailyLimit = AppSettingsStore.DEFAULT_DAILY_LIMIT
    private var dailyLimitSummaryView: TextView? = null
    private val appSettings by lazy { AppSettingsStore(applicationContext) }
    private var currentSummaries: List<MealSummaryEntity> = emptyList()

    private var isDarkTheme = false

    private lateinit var rootLayout: LinearLayout
    private lateinit var titleText: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var pageContainer: LinearLayout
    private lateinit var homeContentViews: List<View>
    private var photoHistory: List<PhotoHistoryEntity> = emptyList()
    private var activitySessions: List<ActivitySessionEntity> = emptyList()
    private var activitySnapshot = ActivitySnapshot()
    private var pendingActivityType = ActivityTrackingService.TYPE_WALK
    private var pendingActivityWeight = 70f
    private var pendingActivityStart = false
    private var locationSettingsRequested = false
    private lateinit var detailsBackCallback: OnBackPressedCallback
    private val reminderSettingsStore by lazy { MealReminderSettingsStore(applicationContext) }
    private val notificationPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> reminderController.onNotificationPermissionResult(granted) }
    private val cameraPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraController.onPermissionResult(granted) }
    private val activityLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            val hasPreciseLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPreciseLocation) {
                pendingActivityStart = false
                Toast.makeText(this, R.string.activity_location_permission, Toast.LENGTH_LONG).show()
            } else {
                activityTrackingRenderer.onLocationPermissionGranted()
                if (pendingActivityStart) {
                    continueActivityStart()
                } else {
                    promptToEnableLocationIfNeeded()
                }
            }
        }
    private val activityRecognitionPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && pendingActivityStart) {
                pendingActivityStart = false
                startActivityTrackingService(pendingActivityType, pendingActivityWeight)
            } else if (!granted) {
                pendingActivityStart = false
                Toast.makeText(this, R.string.activity_sensor_permission, Toast.LENGTH_LONG).show()
            }
        }
    private val exactAlarmSettingsLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { reminderController.onExactAlarmSettingsResult() }
    private val mealRepository by lazy {
        MealRepository(DatabaseProvider.get(applicationContext).mealDao())
    }
    private val cameraController: MealCameraController by lazy {
        MealCameraController(
            activity = this,
            onPhotoCaptured = { imageBytes, note ->
                photoWorkflow.saveCapturedPhoto(imageBytes, note)
            },
            requestCameraPermission = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
    private val scanPageRenderer by lazy {
        ScanPageRenderer(
            activity = this,
            cameraController = cameraController,
            pageText = viewFactory::pageText,
            styledButton = viewFactory::styledButton
        )
    }
    private val dashboardRenderer by lazy { DashboardRenderer(this) { isDarkTheme } }
    private val themeApplier by lazy { AppThemeApplier(this) }
    private val viewFactory by lazy { ActivityViewFactory(this) { isDarkTheme } }
    private val dailyLimitController by lazy {
        DailyLimitSettingsController(
            activity = this,
            currentLimit = { dailyLimit },
            onLimitSaved = ::applyDailyLimit,
            isDarkTheme = { isDarkTheme }
        )
    }
    private val settingsPageRenderer by lazy {
        SettingsPageRenderer(
            activity = this,
            reminderSettingsStore = reminderSettingsStore,
            isDarkTheme = { isDarkTheme },
            pageText = viewFactory::pageText
        )
    }
    private val reminderController: MealReminderController by lazy {
        MealReminderController(
            activity = this,
            settingsStore = reminderSettingsStore,
            isDarkTheme = { isDarkTheme },
            isHomePage = { pageNavigator.currentPage == Page.HOME },
            styledButton = viewFactory::styledButton,
            requestNotificationPermission = {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            },
            requestExactAlarmAccess = {
                exactAlarmSettingsLauncher.launch(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:$packageName")
                    )
                )
            },
            openSettings = { bottomNav.selectedItemId = R.id.nav_settings }
        )
    }
    private val historyRenderer by lazy {
        MealHistoryRenderer(
            activity = this,
            isDarkTheme = { isDarkTheme },
            decodePhoto = { photoWorkflow.decodeOrientedPhoto(it) },
            onPhotoClick = { photo -> pageNavigator.showPhotoDetails(photo, Page.HISTORY) },
            onDeletePhoto = { photoId -> lifecycleScope.launch { mealRepository.deletePhoto(photoId) } }
        )
    }
    private val activityTrackingRenderer by lazy {
        ActivityTrackingRenderer(
            activity = this,
            pageContainer = pageContainer,
            pageText = viewFactory::pageText,
            styledButton = viewFactory::styledButton,
            isDarkTheme = { isDarkTheme },
            onStart = ::requestActivityTracking,
            onStop = { ActivityTrackingService.stop(this) },
            onSessionClick = pageNavigator::showActivityDetails,
            onDeleteSession = { sessionId ->
                lifecycleScope.launch {
                    mealRepository.deleteActivitySession(sessionId)
                    Toast.makeText(this@MainActivity, R.string.activity_deleted, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    private val photoDetailsRenderer: PhotoDetailsRenderer by lazy {
        PhotoDetailsRenderer(
            activity = this,
            isDarkTheme = { isDarkTheme },
            decodePhoto = { photoWorkflow.decodeOrientedPhoto(it) },
            styledButton = viewFactory::styledButton,
            onRetryAnalysis = { photoWorkflow.retryPhotoAnalysis(it) }
        )
    }
    private val photoWorkflow: MealPhotoWorkflow by lazy {
        MealPhotoWorkflow(
            activity = this,
            repository = mealRepository,
            detailsRenderer = photoDetailsRenderer,
            isScanPage = { pageNavigator.currentPage == Page.SCAN },
            onPhotoOpened = { pageNavigator.showPhotoDetails(it, Page.SCAN) },
            onCaptureFinished = { cameraController.refreshCaptureButton() }
        )
    }
    private val pageRenderer: MainPageRenderer by lazy {
        MainPageRenderer(
            activity = this,
            pageContainer = pageContainer,
            scanRenderer = scanPageRenderer,
            historyRenderer = historyRenderer,
            activityTrackingRenderer = activityTrackingRenderer,
            settingsRenderer = settingsPageRenderer,
            reminderController = reminderController,
            dailyLimitController = dailyLimitController,
            pageText = viewFactory::pageText,
            styledButton = viewFactory::styledButton,
            isDarkTheme = { isDarkTheme },
            applyTheme = ::applyCurrentTheme,
            openSettingsDetail = { pageNavigator.openSettingsDetail(it) },
            onBack = ::navigateBackFromDetails,
            onLimitSummaryCreated = { dailyLimitSummaryView = it },
            requestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
        )
    }
    private val pageNavigator: MainPageNavigator by lazy {
        MainPageNavigator(
            activity = this,
            title = titleText,
            pageContainer = pageContainer,
            homeContentViews = homeContentViews,
            beforePageChange = {
                photoWorkflow.cancelDetails()
                if (pageNavigator.currentPage == Page.LIMIT_SETTINGS) dailyLimitController.savePending()
                cameraController.detach()
                if (pageNavigator.currentPage == Page.ACTIVITY ||
                    pageNavigator.currentPage == Page.ACTIVITY_DETAILS
                ) activityTrackingRenderer.onPageHidden()
                pageRenderer.clearDynamicFields()
            },
            onHomeShown = reminderController::onPageChanged,
            renderPage = { page ->
                if (page == Page.DETAILS) {
                    pageNavigator.currentPhoto()?.let { photo ->
                        photoWorkflow.renderDetails(pageContainer, photo) {
                            navigateBackFromDetails()
                        }
                    }
                } else if (page == Page.ACTIVITY_DETAILS) {
                    pageNavigator.currentActivitySession()?.let { session ->
                        activityTrackingRenderer.renderSessionDetails(pageContainer, session)
                    }
                } else {
                    pageRenderer.render(page, photoHistory, dailyLimit, activitySnapshot, activitySessions)
                }
            },
            setDetailsBackEnabled = { detailsBackCallback.isEnabled = it }
        )
    }

    // --- Start aplikacji, konfiguracja widoków i obsługa cyklu życia ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dailyLimit = appSettings.dailyLimit()
        isDarkTheme = appSettings.isDarkThemeEnabled()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        rootLayout = findViewById(R.id.rootLayout)
        titleText = findViewById(R.id.titleText)
        bottomNav = findViewById(R.id.bottomNavigation)

        pageContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(viewFactory.dpValue(16), viewFactory.dpValue(20), viewFactory.dpValue(16), viewFactory.dpValue(16))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            visibility = View.GONE
        }
        rootLayout.addView(pageContainer, 1)
        homeContentViews = (2..7).map { rootLayout.getChildAt(it) }
        reminderController.attachHomeNotice(rootLayout)

        // Przycisk motywu na ekranie głównym zapisuje wybór i odświeża bieżący widok.
        findViewById<ImageButton>(R.id.themeToggle).setOnClickListener {
            val newTheme = !isDarkTheme
            if (!appSettings.saveDarkTheme(newTheme)) {
                Toast.makeText(this, R.string.theme_save_error, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            isDarkTheme = newTheme
            applyCurrentTheme()
            dashboardRenderer.buildChart(currentSummaries)
            pageNavigator.showPage(pageNavigator.currentPage)
        }

        dashboardRenderer.updateDashboard(currentSummaries, dailyLimit)
        applyCurrentTheme()
        dashboardRenderer.buildChart(currentSummaries)

        lifecycleScope.launch {
            mealRepository.observeSummaries().collect { summaries ->
                currentSummaries = summaries
                dashboardRenderer.updateDashboard(currentSummaries, dailyLimit)
                dashboardRenderer.buildChart(currentSummaries)
            }
        }

        lifecycleScope.launch {
            mealRepository.observePhotoHistory().collect { photos ->
                photoHistory = photos
                if (pageNavigator.currentPage == Page.HISTORY) pageRenderer.renderPhotoHistory(photos)
            }
        }

        lifecycleScope.launch {
            mealRepository.observeActivitySessions().collect { sessions ->
                activitySessions = sessions
                if (pageNavigator.currentPage == Page.ACTIVITY) {
                    pageRenderer.updateActivity(activitySnapshot, activitySessions)
                }
            }
        }

        lifecycleScope.launch {
            ActivityTrackingState.snapshots.collect { snapshot ->
                activitySnapshot = snapshot
                if (pageNavigator.currentPage == Page.ACTIVITY) {
                    pageRenderer.updateActivity(activitySnapshot, activitySessions)
                }
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> pageNavigator.showPage(Page.HOME)
                R.id.nav_scan -> pageNavigator.showPage(Page.SCAN)
                R.id.nav_history -> pageNavigator.showPage(Page.HISTORY)
                R.id.nav_activity -> {
                    val shown = pageNavigator.showPage(Page.ACTIVITY)
                    if (shown) ensureActivityLocationPermission()
                    shown
                }
                R.id.nav_settings -> pageNavigator.showPage(Page.SETTINGS)
                else -> false
            }
        }

        detailsBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                navigateBackFromDetails()
            }
        }
        onBackPressedDispatcher.addCallback(this, detailsBackCallback)
    }

    override fun onDestroy() {
        cameraController.detach()
        photoWorkflow.cancelDetails()
        activityTrackingRenderer.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        reminderController.onResume()
        if (pageNavigator.currentPage == Page.ACTIVITY ||
            pageNavigator.currentPage == Page.ACTIVITY_DETAILS
        ) {
            if (pageNavigator.currentPage == Page.ACTIVITY) {
                activityTrackingRenderer.onPageShown()
                if (hasPreciseLocationPermission()) {
                    activityTrackingRenderer.onLocationPermissionGranted()
                }
                if (locationSettingsRequested) {
                    locationSettingsRequested = false
                    if (isGpsEnabled()) {
                        if (pendingActivityStart) continueActivityStart()
                    } else if (pendingActivityStart) {
                        pendingActivityStart = false
                        Toast.makeText(this, R.string.activity_location_error, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                activityTrackingRenderer.onDetailsPageShown()
            }
        }
    }

    override fun onPause() {
        if (pageNavigator.currentPage == Page.ACTIVITY ||
            pageNavigator.currentPage == Page.ACTIVITY_DETAILS
        ) activityTrackingRenderer.onPageHidden()
        super.onPause()
    }

    private fun requestActivityTracking(activityType: String, weightKg: Float) {
        pendingActivityType = activityType
        pendingActivityWeight = weightKg
        pendingActivityStart = true
        if (hasPreciseLocationPermission()) {
            continueActivityStart()
        } else {
            requestActivityLocationPermission()
        }
    }

    private fun ensureActivityLocationPermission() {
        if (hasPreciseLocationPermission()) {
            activityTrackingRenderer.onLocationPermissionGranted()
            promptToEnableLocationIfNeeded()
        } else {
            requestActivityLocationPermission()
        }
    }

    private fun requestActivityLocationPermission() {
        activityLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun continueActivityStart() {
        if (!isGpsEnabled()) {
            promptToEnableLocationIfNeeded()
            return
        }
        if (requiresActivityRecognitionPermission() &&
            checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            pendingActivityStart = false
            startActivityTrackingService(pendingActivityType, pendingActivityWeight)
        }
    }

    private fun promptToEnableLocationIfNeeded() {
        if (isGpsEnabled() || isFinishing || supportFragmentManager.isStateSaved) return
        val dialogTheme = if (isDarkTheme) R.style.AppTheme_Dialog_Dark else R.style.AppTheme_Dialog_Light
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this, dialogTheme)
            .setTitle(R.string.activity_location_enable_title)
            .setMessage(R.string.activity_location_enable_message)
            .setPositiveButton(R.string.activity_location_enable_button) { _, _ ->
                locationSettingsRequested = true
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun hasPreciseLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun isGpsEnabled(): Boolean =
        (getSystemService(LOCATION_SERVICE) as LocationManager)
            .isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun requiresActivityRecognitionPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val sensors = getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        return sensors.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) != null
    }

    private fun startActivityTrackingService(activityType: String, weightKg: Float) {
        try {
            ActivityTrackingService.start(this, activityType, weightKg)
        } catch (exception: SecurityException) {
            Toast.makeText(this, R.string.activity_location_permission, Toast.LENGTH_LONG).show()
        } catch (exception: IllegalStateException) {
            Toast.makeText(this, R.string.activity_location_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateBackFromDetails() {
        pageNavigator.navigateBackFromDetails()
        if (pageNavigator.currentPage == Page.ACTIVITY) {
            activityTrackingRenderer.onPageShown()
            if (hasPreciseLocationPermission()) {
                activityTrackingRenderer.onLocationPermissionGranted()
            }
        }
    }

    private fun applyDailyLimit(newLimit: Int) {
        dailyLimit = newLimit
        appSettings.saveDailyLimit(newLimit)
        dashboardRenderer.updateDashboard(currentSummaries, dailyLimit)
        dailyLimitSummaryView?.text = getString(R.string.settings_daily_limit_summary, newLimit)
    }

    // --- Wspólny tekst i przyciski Material oraz dekodowanie orientacji zdjęcia ---
    // Przekazuje dynamicznie utworzone pola, aby motyw obejmował również formularz otwartego ekranu.
    private fun applyCurrentTheme() {
        themeApplier.apply(
            isDarkTheme = isDarkTheme,
            reminderHomeNotice = reminderController.homeNotice,
            inputLayouts = listOf(pageRenderer.currentInputLayout())
        )
    }

}
