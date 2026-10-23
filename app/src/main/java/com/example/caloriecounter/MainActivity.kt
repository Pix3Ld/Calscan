package com.example.caloriecounter

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.camera.MealCameraController
import com.example.caloriecounter.camera.ScanPageRenderer
import androidx.lifecycle.lifecycleScope
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.caloriecounter.data.DatabaseProvider
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
    private lateinit var detailsBackCallback: OnBackPressedCallback
    private val reminderSettingsStore by lazy { MealReminderSettingsStore(applicationContext) }
    private val notificationPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> reminderController.onNotificationPermissionResult(granted) }
    private val cameraPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraController.onPermissionResult(granted) }
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
            settingsRenderer = settingsPageRenderer,
            reminderController = reminderController,
            dailyLimitController = dailyLimitController,
            pageText = viewFactory::pageText,
            styledButton = viewFactory::styledButton,
            isDarkTheme = { isDarkTheme },
            applyTheme = ::applyCurrentTheme,
            openSettingsDetail = { pageNavigator.openSettingsDetail(it) },
            onBack = { pageNavigator.navigateBackFromDetails() },
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
                pageRenderer.clearDynamicFields()
            },
            onHomeShown = reminderController::onPageChanged,
            renderPage = { page ->
                if (page == Page.DETAILS) {
                    pageNavigator.currentPhoto()?.let { photo ->
                        photoWorkflow.renderDetails(pageContainer, photo) {
                            pageNavigator.navigateBackFromDetails()
                        }
                    }
                } else {
                    pageRenderer.render(page, photoHistory, dailyLimit)
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

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> pageNavigator.showPage(Page.HOME)
                R.id.nav_scan -> pageNavigator.showPage(Page.SCAN)
                R.id.nav_history -> pageNavigator.showPage(Page.HISTORY)
                R.id.nav_settings -> pageNavigator.showPage(Page.SETTINGS)
                else -> false
            }
        }

        detailsBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                pageNavigator.navigateBackFromDetails()
            }
        }
        onBackPressedDispatcher.addCallback(this, detailsBackCallback)
    }

    override fun onDestroy() {
        cameraController.detach()
        photoWorkflow.cancelDetails()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        reminderController.onResume()
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
