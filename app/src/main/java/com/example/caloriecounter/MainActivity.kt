package com.example.caloriecounter

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.SurfaceTexture
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.os.Bundle
import android.os.Build
import android.net.Uri
import android.provider.Settings
import android.text.InputType
import android.text.Editable
import android.text.TextWatcher
import android.view.Surface
import android.view.TextureView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.content.pm.PackageManager
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.caloriecounter.data.DatabaseProvider
import com.example.caloriecounter.data.MealRepository
import com.example.caloriecounter.data.PhotoHistoryEntity
import com.example.caloriecounter.reminders.MealReminderScheduler
import com.example.caloriecounter.reminders.MealReminderSettings
import com.example.caloriecounter.reminders.MealReminderSettingsStore
import com.example.caloriecounter.reminders.ReminderTimeCalculator
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val dailyLimit = 2200
    private val todayConsumed = 1545
    private val yesterdayConsumed = 1880
    private val weekCalories = listOf(1800, 2100, 1650, 1950, 1480, 1730, 1545)
    private val proteinValue = 78
    private val carbsValue = 190
    private val fatValue = 60

    private var isDarkTheme = false

    private lateinit var rootLayout: LinearLayout
    private lateinit var titleText: TextView
    private lateinit var chartTitle: TextView
    private lateinit var themeToggle: ImageButton
    private lateinit var todayValue: TextView
    private lateinit var yesterdayValue: TextView
    private lateinit var weekValue: TextView
    private lateinit var remainingValue: TextView
    private lateinit var chartContainer: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var proteinDot: View
    private lateinit var carbsDot: View
    private lateinit var fatDot: View
    private lateinit var proteinLabel: TextView
    private lateinit var carbsLabel: TextView
    private lateinit var fatLabel: TextView
    private lateinit var pageContainer: LinearLayout
    private lateinit var homeContentViews: List<View>
    private var cameraDevice: CameraDevice? = null
    private var cameraSession: CameraCaptureSession? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null
    private var cameraTexture: TextureView? = null
    private var photoImageReader: ImageReader? = null
    private var scanDetailsInputLayout: TextInputLayout? = null
    private var scanDetailsEditText: TextInputEditText? = null
    private var cameraPlaceholder: Button? = null
    private var captureButton: Button? = null
    private var historyContentContainer: LinearLayout? = null
    private var photoHistory: List<PhotoHistoryEntity> = emptyList()
    private var cameraEnabled = true
    private var pendingPhotoNote: String? = null
    private var currentPage = Page.HOME
    private var detailsReturnPage = Page.HISTORY
    private lateinit var detailsBackCallback: OnBackPressedCallback
    private var reminderSwitch: Switch? = null
    private var reminderOptions: LinearLayout? = null
    private var reminderHomeNotice: TextView? = null
    private var updatingReminderSwitch = false
    private var pendingReminderEnable = false
    private val reminderSettingsStore by lazy { MealReminderSettingsStore(applicationContext) }
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) continueReminderEnableFlow() else rejectReminderEnable()
    }
    private val exactAlarmSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!pendingReminderEnable) return@registerForActivityResult
        if (MealReminderScheduler.hasExactAlarmAccess(this)) {
            continueReminderEnableFlow()
        } else {
            rejectReminderEnable()
        }
    }
    private val mealRepository by lazy {
        MealRepository(DatabaseProvider.get(applicationContext).mealDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        rootLayout = findViewById(R.id.rootLayout)
        titleText = findViewById(R.id.titleText)
        chartTitle = findViewById(R.id.chartTitle)
        themeToggle = findViewById(R.id.themeToggle)
        todayValue = findViewById(R.id.todayValue)
        yesterdayValue = findViewById(R.id.yesterdayValue)
        weekValue = findViewById(R.id.weekValue)
        remainingValue = findViewById(R.id.remainingValue)
        chartContainer = findViewById(R.id.chartContainer)
        bottomNav = findViewById(R.id.bottomNavigation)
        proteinDot = findViewById(R.id.proteinDot)
        carbsDot = findViewById(R.id.carbsDot)
        fatDot = findViewById(R.id.fatDot)
        proteinLabel = findViewById(R.id.proteinLabel)
        carbsLabel = findViewById(R.id.carbsLabel)
        fatLabel = findViewById(R.id.fatLabel)

        pageContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp(), 20.dp(), 16.dp(), 16.dp())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            visibility = View.GONE
        }
        rootLayout.addView(pageContainer, 1)
        homeContentViews = (2..7).map { rootLayout.getChildAt(it) }
        initializeHomeReminderNotice()

        themeToggle.setOnClickListener {
            isDarkTheme = !isDarkTheme
            updateTheme()
            buildChart()
        }

        updateDashboard()
        updateTheme()
        buildChart()

        lifecycleScope.launch {
            mealRepository.observePhotoHistory().collect { photos ->
                photoHistory = photos
                if (currentPage == Page.HISTORY) renderPhotoHistory(photos)
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showPage(Page.HOME)
                R.id.nav_scan -> showPage(Page.SCAN)
                R.id.nav_history -> showPage(Page.HISTORY)
                R.id.nav_settings -> showPage(Page.SETTINGS)
                else -> false
            }
        }

        detailsBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                showPage(detailsReturnPage)
            }
        }
        onBackPressedDispatcher.addCallback(this, detailsBackCallback)
    }

    override fun onDestroy() {
        closeCamera()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (!pendingReminderEnable) {
            val settings = reminderSettingsStore.load()
            if (settings.enabled) {
                if (!reminderPermissionsGranted()) {
                    reminderSettingsStore.save(settings.copy(enabled = false))
                    MealReminderScheduler.cancelAll(this)
                    setReminderSwitchChecked(false)
                    reminderOptions?.visibility = View.GONE
                    Toast.makeText(this, R.string.reminder_permissions_required, Toast.LENGTH_LONG).show()
                } else {
                    MealReminderScheduler.scheduleAll(this)
                }
            }
        }
        updateHomeReminderNotice()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                if (cameraEnabled && cameraTexture?.isAvailable == true) openCamera()
            } else {
                cameraEnabled = false
                cameraTexture?.visibility = View.GONE
                cameraPlaceholder?.visibility = View.VISIBLE
            }
        }
    }

    private enum class Page { HOME, SCAN, HISTORY, SETTINGS, DETAILS }

    private fun showPage(page: Page): Boolean {
        detailsBackCallback.isEnabled = false
        closeCamera()
        scanDetailsInputLayout = null
        scanDetailsEditText = null
        cameraPlaceholder = null
        cameraEnabled = true
        currentPage = page
        if (page == Page.HOME) {
            pageContainer.visibility = View.GONE
            homeContentViews.forEach { it.visibility = View.VISIBLE }
            updateHomeReminderNotice()
            titleText.text = getString(R.string.title_dashboard)
            return true
        }

        reminderHomeNotice?.visibility = View.GONE
        homeContentViews.forEach { it.visibility = View.GONE }
        pageContainer.visibility = View.VISIBLE
        pageContainer.removeAllViews()
        when (page) {
            Page.SCAN -> {
                titleText.text = getString(R.string.title_scan)
                buildScanPage()
            }
            Page.HISTORY -> {
                titleText.text = getString(R.string.title_history)
                buildHistoryPage()
            }
            Page.SETTINGS -> {
                titleText.text = getString(R.string.title_settings)
                buildSettingsPage()
            }
            Page.HOME, Page.DETAILS -> Unit
        }
        return true
    }

    private fun showPhotoDetails(photo: PhotoHistoryEntity, returnPage: Page) {
        closeCamera()
        scanDetailsInputLayout = null
        scanDetailsEditText = null
        cameraPlaceholder = null
        captureButton = null
        detailsReturnPage = returnPage
        currentPage = Page.DETAILS
        detailsBackCallback.isEnabled = true
        homeContentViews.forEach { it.visibility = View.GONE }
        pageContainer.visibility = View.VISIBLE
        pageContainer.removeAllViews()
        titleText.text = getString(R.string.title_details)
        buildPhotoDetails(photo)
    }

    private fun buildPhotoDetails(photo: PhotoHistoryEntity) {
        pageContainer.addView(Button(this).apply {
            text = getString(R.string.details_back)
            isAllCaps = false
            setOnClickListener { showPage(detailsReturnPage) }
        })

        val scrollView = android.widget.ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8.dp(), 0, 16.dp())
        }
        val photoView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300.dp()
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            setImageBitmap(decodeOrientedPhoto(photo.imageBytes))
            contentDescription = getString(R.string.history_photo_content_description)
        }
        content.addView(photoView)
        content.addView(pageText(
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(photo.createdAt)),
            13f,
            false
        ).apply { setPadding(0, 12.dp(), 0, 0) })
        photo.note?.let { note ->
            content.addView(pageText(note, 16f, true).apply { setPadding(0, 8.dp(), 0, 0) })
        }

        content.addView(pageText(getString(R.string.details_mock_notice), 13f, false).apply {
            setPadding(0, 20.dp(), 0, 8.dp())
        })
        content.addView(pageText(getString(R.string.details_mock_meal_name), 22f, true))
        content.addView(pageText(getString(R.string.details_mock_calories), 28f, true).apply {
            setPadding(0, 4.dp(), 0, 12.dp())
        })
        listOf(
            R.string.details_protein,
            R.string.details_carbohydrates,
            R.string.details_fat,
            R.string.details_fiber,
            R.string.details_vitamins
        ).forEach { value ->
            content.addView(pageText(getString(value), 16f, false).apply {
                setPadding(0, 6.dp(), 0, 6.dp())
            })
        }
        scrollView.addView(content)
        pageContainer.addView(scrollView)
    }

    private fun buildScanPage() {
        val intro = pageText(getString(R.string.scan_page_intro), 16f, true)
        pageContainer.addView(intro)

        val cameraFrame = object : FrameLayout(this) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val width = MeasureSpec.getSize(widthMeasureSpec)
                val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
                var measuredWidth = width
                var measuredHeight = measuredWidth * 9 / 8

                if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED && measuredHeight > maxHeight) {
                    measuredHeight = maxHeight
                    measuredWidth = measuredHeight * 8 / 9
                }

                setMeasuredDimension(measuredWidth, measuredHeight)
                measureChildren(
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
                )
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16.dp(), 0, 16.dp()) }
            setBackgroundColor(Color.BLACK)
        }
        cameraTexture = TextureView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isOpaque = true
            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                    if (cameraEnabled) openCamera()
                }
                override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) = Unit
                override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                    if (cameraTexture?.surfaceTexture === surface) closeCamera()
                    return true
                }
                override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) = Unit
            }
        }
        cameraFrame.addView(cameraTexture)
        cameraPlaceholder = Button(this).apply {
            text = getString(R.string.camera_enable_button)
            isAllCaps = false
            visibility = View.GONE
            setOnClickListener { enableCameraPreview() }
        }
        cameraFrame.addView(cameraPlaceholder, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))
        pageContainer.addView(cameraFrame)

        val scanButton = Button(this).apply {
            text = getString(R.string.scan_button)
            isAllCaps = false
            textSize = 18f
            minHeight = 64.dp()
            isEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                64.dp()
            ).apply { setMargins(0, 8.dp(), 0, 12.dp()) }
            setOnClickListener { capturePhoto() }
        }
        captureButton = scanButton
        pageContainer.addView(scanButton)

        val detailsInput = TextInputLayout(this).apply {
            hint = getString(R.string.scan_details_hint)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(TextInputEditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
                minLines = 1
                maxLines = 2
                setPadding(0, 8.dp(), 0, 8.dp())
                scanDetailsEditText = this
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) disableCameraForNote()
                }
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit
                    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                        if (!text.isNullOrEmpty()) disableCameraForNote()
                    }
                    override fun afterTextChanged(editable: Editable?) = Unit
                })
            })
        }
        scanDetailsInputLayout = detailsInput
        pageContainer.addView(detailsInput)
        updateTheme()

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun buildHistoryPage() {
        pageContainer.addView(pageText(getString(R.string.history_page_intro), 16f, true))
        val scrollView = android.widget.ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        historyContentContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scrollView.addView(historyContentContainer)
        pageContainer.addView(scrollView)
        renderPhotoHistory(photoHistory)
    }

    private fun renderPhotoHistory(photos: List<PhotoHistoryEntity>) {
        val container = historyContentContainer ?: return
        container.removeAllViews()
        if (photos.isEmpty()) {
            container.addView(pageText(getString(R.string.history_empty), 15f, false).apply {
                setPadding(0, 24.dp(), 0, 0)
            })
            return
        }

        photos.forEach { photo ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(10.dp(), 10.dp(), 8.dp(), 10.dp())
                background = ColorDrawable(if (isDarkTheme) Color.parseColor("#1F2937") else Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 12.dp(), 0, 0) }
            }
            val thumbnail = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(76.dp(), 76.dp())
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(decodeOrientedPhoto(photo.imageBytes))
                contentDescription = getString(R.string.history_photo_content_description)
            }
            row.addView(thumbnail)

            val details = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { setMargins(12.dp(), 0, 4.dp(), 0) }
            }
            details.addView(pageText(
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(photo.createdAt)),
                12f,
                false
            ))
            details.addView(pageText(photo.note ?: getString(R.string.history_photo_default), 15f, true))
            row.addView(details)
            row.isClickable = true
            row.isFocusable = true
            row.setOnClickListener { showPhotoDetails(photo, Page.HISTORY) }

            val deleteButton = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                contentDescription = getString(R.string.history_delete)
                background = ColorDrawable(Color.TRANSPARENT)
                setOnClickListener {
                    lifecycleScope.launch { mealRepository.deletePhoto(photo.id) }
                }
            }
            row.addView(deleteButton, LinearLayout.LayoutParams(44.dp(), 44.dp()))
            container.addView(row)
        }
    }

    private fun buildSettingsPage() {
        pageContainer.addView(pageText(getString(R.string.settings_page_intro), 16f, true))
        val settings = reminderSettingsStore.load()
        val notifications = Switch(this).apply {
            text = getString(R.string.settings_notifications)
            isChecked = settings.enabled
            setTextColor(if (isDarkTheme) Color.WHITE else Color.parseColor("#1C2430"))
        }
        reminderSwitch = notifications
        notifications.setOnCheckedChangeListener { _, checked ->
            if (updatingReminderSwitch) return@setOnCheckedChangeListener
            if (checked) {
                setReminderSwitchChecked(false)
                beginReminderEnable(notifications)
            } else {
                disableMealReminders()
            }
        }
        pageContainer.addView(notifications)
        reminderOptions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = if (settings.enabled) View.VISIBLE else View.GONE
        }
        pageContainer.addView(reminderOptions)
        renderReminderOptions()
        val dailyLimit = Button(this).apply {
            text = getString(R.string.settings_daily_limit)
            setOnClickListener { }
        }
        pageContainer.addView(dailyLimit)
    }

    private fun initializeHomeReminderNotice() {
        reminderHomeNotice = TextView(this).apply {
            text = getString(R.string.home_reminder_notice)
            textSize = 14f
            setTextColor(Color.parseColor("#1C5DD9"))
            setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
            background = GradientDrawable().apply {
                cornerRadius = 8.dp().toFloat()
                setColor(Color.parseColor("#E6F0FF"))
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { bottomNav.selectedItemId = R.id.nav_settings }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16.dp(), 12.dp(), 16.dp(), 0) }
        }
        rootLayout.addView(reminderHomeNotice, 2)
        updateHomeReminderNotice()
    }

    private fun updateHomeReminderNotice() {
        reminderHomeNotice?.visibility = if (
            currentPage == Page.HOME && !reminderSettingsStore.load().enabled
        ) View.VISIBLE else View.GONE
    }

    private fun beginReminderEnable(toggle: Switch) {
        reminderSwitch = toggle
        pendingReminderEnable = true
        continueReminderEnableFlow()
    }

    private fun continueReminderEnableFlow() {
        if (!pendingReminderEnable) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        if (!MealReminderScheduler.hasExactAlarmAccess(this)) {
            val request = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:$packageName")
            )
            exactAlarmSettingsLauncher.launch(request)
            return
        }

        pendingReminderEnable = false
        val settings = reminderSettingsStore.load()
        if (settings.hasConfiguredMeals) {
            enableMealReminders(settings)
        } else {
            showMealCountOptIn()
        }
    }

    private fun showMealCountOptIn() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reminder_choose_meal_count_title)
            .setMessage(R.string.reminder_choose_meal_count_message)
            .setPositiveButton(R.string.reminder_choose_meal_count) { _, _ -> showMealCountPicker(null) }
            .setNegativeButton(R.string.reminder_keep_default) { _, _ ->
                val settings = reminderSettingsStore.load()
                enableMealReminders(
                    settings.copy(
                        mealTimesMinutes = ReminderTimeCalculator.defaultTimes(settings.mealCount),
                        hasConfiguredMeals = true
                    )
                )
            }
            .setOnCancelListener { setReminderSwitchChecked(false) }
            .show()
    }

    private fun showMealCountPicker(currentCount: Int?) {
        val picker = NumberPicker(this).apply {
            minValue = ReminderTimeCalculator.MIN_MEALS
            maxValue = ReminderTimeCalculator.MAX_MEALS
            value = currentCount ?: ReminderTimeCalculator.DEFAULT_MEALS
            wrapSelectorWheel = false
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.reminder_meal_count_title)
            .setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val oldSettings = reminderSettingsStore.load()
                val newSettings = oldSettings.copy(
                    mealCount = picker.value,
                    mealTimesMinutes = ReminderTimeCalculator.defaultTimes(picker.value),
                    hasConfiguredMeals = true
                )
                if (oldSettings.enabled) {
                    saveMealReminderSettings(newSettings)
                } else {
                    enableMealReminders(newSettings)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                if (currentCount == null) setReminderSwitchChecked(false)
            }
            .setOnCancelListener {
                if (currentCount == null) setReminderSwitchChecked(false)
            }
            .show()
    }

    private fun enableMealReminders(settings: MealReminderSettings) {
        if (!reminderPermissionsGranted()) {
            setReminderSwitchChecked(false)
            Toast.makeText(this, R.string.reminder_permissions_required, Toast.LENGTH_LONG).show()
            return
        }
        saveMealReminderSettings(settings.copy(enabled = true, hasConfiguredMeals = true))
    }

    private fun disableMealReminders() {
        pendingReminderEnable = false
        reminderSettingsStore.save(reminderSettingsStore.load().copy(enabled = false))
        MealReminderScheduler.cancelAll(this)
        reminderOptions?.visibility = View.GONE
        updateHomeReminderNotice()
    }

    private fun saveMealReminderSettings(settings: MealReminderSettings) {
        reminderSettingsStore.save(settings)
        if (settings.enabled) MealReminderScheduler.scheduleAll(this) else MealReminderScheduler.cancelAll(this)
        setReminderSwitchChecked(settings.enabled)
        reminderOptions?.visibility = if (settings.enabled) View.VISIBLE else View.GONE
        renderReminderOptions()
        updateHomeReminderNotice()
    }

    private fun renderReminderOptions() {
        val container = reminderOptions ?: return
        val settings = reminderSettingsStore.load()
        container.removeAllViews()

        container.addView(Button(this).apply {
            text = getString(R.string.reminder_meal_count_label, settings.mealCount)
            setOnClickListener { showMealCountPicker(settings.mealCount) }
        })

        settings.mealTimesMinutes.forEachIndexed { index, minutes ->
            container.addView(Button(this).apply {
                text = getString(R.string.reminder_meal_time_label, index + 1, formatMealTime(minutes))
                setOnClickListener {
                    TimePickerDialog(
                        this@MainActivity,
                        { _, hour, minute ->
                            val updatedTimes = settings.mealTimesMinutes.toMutableList()
                            updatedTimes[index] = hour * 60 + minute
                            saveMealReminderSettings(settings.copy(mealTimesMinutes = updatedTimes))
                        },
                        minutes / 60,
                        minutes % 60,
                        true
                    ).show()
                }
            })
        }
    }

    private fun formatMealTime(minutes: Int): String =
        String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)

    private fun setReminderSwitchChecked(checked: Boolean) {
        updatingReminderSwitch = true
        reminderSwitch?.isChecked = checked
        updatingReminderSwitch = false
    }

    private fun reminderPermissionsGranted(): Boolean {
        val notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return notificationsGranted && MealReminderScheduler.hasExactAlarmAccess(this)
    }

    private fun rejectReminderEnable() {
        pendingReminderEnable = false
        setReminderSwitchChecked(false)
        Toast.makeText(this, R.string.reminder_permissions_required, Toast.LENGTH_LONG).show()
    }

    private fun pageText(text: String, size: Float, bold: Boolean): TextView = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun decodeOrientedPhoto(imageBytes: ByteArray): android.graphics.Bitmap? {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null
        val rotationDegrees = try {
            ExifInterface(ByteArrayInputStream(imageBytes)).rotationDegrees
        } catch (_: Exception) {
            0
        }
        if (rotationDegrees == 0) return bitmap

        val rotatedBitmap = android.graphics.Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            Matrix().apply { postRotate(rotationDegrees.toFloat()) },
            true
        )
        if (rotatedBitmap !== bitmap) bitmap.recycle()
        return rotatedBitmap
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (!cameraEnabled || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
        val texture = cameraTexture ?: return
        if (!texture.isAvailable || cameraDevice != null || cameraThread != null) return

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = try {
            manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
            } ?: manager.cameraIdList.firstOrNull()
        } catch (_: Exception) {
            null
        } ?: return

        val previewSize = getPreviewSize(manager, cameraId, texture.width)
        val surfaceTexture = texture.surfaceTexture ?: return
        previewSize?.let { surfaceTexture.setDefaultBufferSize(it.width, it.height) }

        cameraThread = HandlerThread("camera").also { it.start() }
        cameraHandler = Handler(cameraThread!!.looper)
        val jpegSize = getJpegSize(manager, cameraId)
        if (jpegSize == null) {
            closeCamera()
            return
        }
        photoImageReader = ImageReader.newInstance(jpegSize.width, jpegSize.height, ImageFormat.JPEG, 2).also { reader ->
            reader.setOnImageAvailableListener({ imageReader ->
                val image = try {
                    imageReader.acquireNextImage()
                } catch (_: Exception) {
                    null
                } ?: return@setOnImageAvailableListener
                val bytes = try {
                    val buffer = image.planes[0].buffer
                    ByteArray(buffer.remaining()).also(buffer::get)
                } finally {
                    image.close()
                }
                saveCapturedPhoto(bytes, pendingPhotoNote)
            }, cameraHandler)
        }
        try {
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    if (cameraTexture !== texture || !texture.isAvailable || !cameraEnabled || isFinishing || isDestroyed) {
                        camera.close()
                        return
                    }
                    try {
                        cameraDevice = camera
                        val activeSurfaceTexture = texture.surfaceTexture ?: run {
                            camera.close()
                            return
                        }
                        val surface = Surface(activeSurfaceTexture)
                        val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                            addTarget(surface)
                        }
                        val imageSurface = photoImageReader?.surface ?: run {
                            camera.close()
                            return
                        }
                        camera.createCaptureSession(listOf(surface, imageSurface), object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                if (cameraDevice !== camera || !cameraEnabled || isFinishing || isDestroyed) {
                                    session.close()
                                    camera.close()
                                    return
                                }
                                cameraSession = session
                                try {
                                    session.setRepeatingRequest(request.build(), null, cameraHandler)
                                    runOnUiThread { captureButton?.isEnabled = true }
                                } catch (_: Exception) {
                                    closeCamera()
                                }
                            }
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                if (cameraDevice === camera) {
                                    closeCamera()
                                    showCameraUnavailable()
                                }
                            }
                        }, cameraHandler)
                    } catch (_: Exception) {
                        camera.close()
                        cameraDevice = null
                    }
                }
                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    if (cameraDevice === camera) {
                        closeCamera()
                        showCameraUnavailable()
                    }
                }
                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    if (cameraDevice === camera) {
                        closeCamera()
                        showCameraUnavailable()
                    }
                }
            }, cameraHandler)
        } catch (_: Exception) {
            closeCamera()
        }
    }

    private fun getJpegSize(manager: CameraManager, cameraId: String): Size? {
        val sizes = try {
            manager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.JPEG)
                ?.toList()
                .orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        return sizes.filter { it.width <= 1600 }
            .maxByOrNull { it.width.toLong() * it.height }
            ?: sizes.maxByOrNull { it.width.toLong() * it.height }
    }

    private fun capturePhoto() {
        val camera = cameraDevice ?: return
        val session = cameraSession ?: return
        val reader = photoImageReader ?: return
        pendingPhotoNote = scanDetailsEditText?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        captureButton?.isEnabled = false
        try {
            val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(reader.surface)
                set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(camera.id))
            }
            session.capture(request.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    runOnUiThread {
                        captureButton?.isEnabled = cameraEnabled
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            R.string.camera_capture_error,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }, cameraHandler)
        } catch (_: Exception) {
            captureButton?.isEnabled = cameraEnabled
            android.widget.Toast.makeText(this, R.string.camera_capture_error, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCapturedPhoto(imageBytes: ByteArray, note: String?) {
        lifecycleScope.launch {
            try {
                val photo = mealRepository.savePhoto(imageBytes, note)
                android.widget.Toast.makeText(
                    this@MainActivity,
                    R.string.photo_saved,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                if (currentPage == Page.SCAN) showPhotoDetails(photo, Page.SCAN)
            } catch (_: Exception) {
                android.widget.Toast.makeText(
                    this@MainActivity,
                    R.string.photo_save_error,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } finally {
                captureButton?.isEnabled = cameraEnabled && cameraSession != null
            }
        }
    }

    private fun getJpegOrientation(cameraId: String): Int {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = try {
            manager.getCameraCharacteristics(cameraId)
        } catch (_: Exception) {
            return 0
        }
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: return 0
        val deviceOrientation = when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val isFrontFacing = characteristics.get(CameraCharacteristics.LENS_FACING) ==
            CameraCharacteristics.LENS_FACING_FRONT
        return if (isFrontFacing) {
            (sensorOrientation + deviceOrientation) % 360
        } else {
            (sensorOrientation - deviceOrientation + 360) % 360
        }
    }

    private fun disableCameraForNote() {
        if (!cameraEnabled) return
        cameraEnabled = false
        closeCamera()
        cameraTexture?.visibility = View.GONE
        cameraPlaceholder?.visibility = View.VISIBLE
        captureButton?.isEnabled = false
    }

    private fun enableCameraPreview() {
        cameraEnabled = true
        cameraPlaceholder?.visibility = View.GONE
        cameraTexture?.visibility = View.VISIBLE
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraTexture?.isAvailable == true) openCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun showCameraUnavailable() {
        runOnUiThread {
            cameraEnabled = false
            cameraTexture?.visibility = View.GONE
            cameraPlaceholder?.visibility = View.VISIBLE
            captureButton?.isEnabled = false
        }
    }

    private fun getPreviewSize(manager: CameraManager, cameraId: String, viewWidth: Int): Size? {
        val sizes = try {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            map?.getOutputSizes(SurfaceTexture::class.java)?.toList().orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        if (sizes.isEmpty()) return null

        val fourByThreeSizes = sizes.filter { it.width * 3 == it.height * 4 }
        val candidates = fourByThreeSizes.ifEmpty { sizes }
        val targetWidth = viewWidth.takeIf { it > 0 } ?: 1080
        return candidates
            .filter { it.width >= targetWidth }
            .minByOrNull { it.width }
            ?: candidates.maxByOrNull { it.width.toLong() * it.height }
    }

    private fun closeCamera() {
        cameraSession?.close()
        cameraDevice?.close()
        photoImageReader?.setOnImageAvailableListener(null, null)
        photoImageReader?.close()
        photoImageReader = null
        cameraSession = null
        cameraDevice = null
        cameraThread?.quitSafely()
        cameraThread = null
        cameraHandler = null
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }

    private fun updateDashboard() {
        val remaining = dailyLimit - todayConsumed

        todayValue.text = getString(R.string.today_value_template, todayConsumed)
        yesterdayValue.text = getString(R.string.yesterday_value_template, yesterdayConsumed)
        weekValue.text = getString(R.string.week_value_template, weekCalories.sum())
        remainingValue.text = getString(R.string.remaining_value_template, remaining)
        proteinLabel.text = getString(R.string.macro_protein_template, proteinValue)
        carbsLabel.text = getString(R.string.macro_carbs_template, carbsValue)
        fatLabel.text = getString(R.string.macro_fat_template, fatValue)
    }

    private fun updateTheme() {
        val backgroundColor = if (isDarkTheme) Color.parseColor("#121826") else Color.parseColor("#F5F7FA")
        val cardColor = if (isDarkTheme) Color.parseColor("#1F2937") else Color.parseColor("#FFFFFF")
        val primaryTextColor = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
        val secondaryTextColor = if (isDarkTheme) Color.parseColor("#D1D5DB") else Color.parseColor("#697586")
        val positiveColor = if (isDarkTheme) Color.parseColor("#6EE7B7") else Color.parseColor("#2E7D32")
        val buttonBackground = if (isDarkTheme) Color.parseColor("#2F3B52") else Color.parseColor("#E5E7EB")
        val buttonIconColor = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#111827")
        val chartCardColor = if (isDarkTheme) Color.parseColor("#243044") else Color.parseColor("#FFFFFF")
        val navBg = if (isDarkTheme) Color.parseColor("#1B2738") else Color.parseColor("#E8EEF5")
        val navItemColor = if (isDarkTheme) Color.parseColor("#E5E7EB") else Color.parseColor("#2E4057")
        val accentBlue = Color.parseColor("#39A0ED")

        window.statusBarColor = backgroundColor
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkTheme
        }

        rootLayout.setBackgroundColor(backgroundColor)
        titleText.setTextColor(primaryTextColor)
        chartTitle.setTextColor(primaryTextColor)

        val toggleBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18f
            setColor(buttonBackground)
            setStroke(1, if (isDarkTheme) Color.parseColor("#3A4657") else Color.parseColor("#D1D5DB"))
        }
        themeToggle.background = toggleBackground
        themeToggle.elevation = 2f
        themeToggle.setColorFilter(buttonIconColor)
        themeToggle.setImageResource(if (isDarkTheme) R.drawable.ic_moon_24 else R.drawable.ic_sun_24)

        val cards = listOf(
            findViewById<View>(R.id.todayCard),
            findViewById<View>(R.id.remainingCard),
            findViewById<View>(R.id.yesterdayCard),
            findViewById<View>(R.id.weekCard),
            findViewById<View>(R.id.chartCard),
            findViewById<View>(R.id.bottomNavigation)
        )

        cards.forEach { it.setBackgroundColor(if (it.id == R.id.bottomNavigation) cardColor else cardColor) }
        findViewById<View>(R.id.chartCard).setBackgroundColor(chartCardColor)
        bottomNav.setBackgroundColor(navBg)
        bottomNav.itemIconTintList = ColorStateList.valueOf(navItemColor)
        bottomNav.itemTextColor = ColorStateList.valueOf(navItemColor)

        todayValue.setTextColor(primaryTextColor)
        yesterdayValue.setTextColor(primaryTextColor)
        weekValue.setTextColor(primaryTextColor)
        remainingValue.setTextColor(positiveColor)

        val labelViews = listOf(findViewById<TextView>(R.id.labelToday), findViewById<TextView>(R.id.labelRemaining), findViewById<TextView>(R.id.labelYesterday), findViewById<TextView>(R.id.labelWeek))
        labelViews.forEach { it.setTextColor(secondaryTextColor) }

        val macroTextColor = if (isDarkTheme) Color.parseColor("#E5E7EB") else Color.parseColor("#1F2937")
        proteinLabel.setTextColor(macroTextColor)
        carbsLabel.setTextColor(macroTextColor)
        fatLabel.setTextColor(macroTextColor)

        proteinDot.setBackgroundColor(Color.parseColor("#E53935"))
        carbsDot.setBackgroundColor(accentBlue)
        fatDot.setBackgroundColor(Color.parseColor("#FF9800"))

        findViewById<TextView>(R.id.scanHint).setTextColor(if (isDarkTheme) Color.parseColor("#B5E0FF") else Color.parseColor("#1C5DD9"))
        findViewById<View>(R.id.scanHint).setBackgroundColor(if (isDarkTheme) Color.parseColor("#1B3A5B") else Color.parseColor("#E6F0FF"))

        scanDetailsInputLayout?.apply {
            val fieldBackground = if (isDarkTheme) Color.parseColor("#1F2937") else Color.WHITE
            val fieldText = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
            val fieldHint = if (isDarkTheme) Color.parseColor("#D1D5DB") else Color.parseColor("#58677A")
            val fieldStroke = if (isDarkTheme) Color.parseColor("#8291A5") else Color.parseColor("#65758B")

            setBoxBackgroundColor(fieldBackground)
            boxStrokeColor = fieldStroke
            boxStrokeWidth = 1.dp()
            boxStrokeWidthFocused = 2.dp()
            defaultHintTextColor = ColorStateList.valueOf(fieldHint)
            editText?.apply {
                setTextColor(fieldText)
                setHintTextColor(fieldHint)
            }
        }
    }

    private fun buildChart() {
        val maxValue = weekCalories.maxOrNull() ?: 1
        chartContainer.removeAllViews()

        val days = listOf("P", "W", "Ś", "C", "P", "S", "N")
        val macroMix = listOf(
            Triple(78, 190, 60),
            Triple(74, 210, 58),
            Triple(80, 170, 62),
            Triple(76, 200, 65),
            Triple(70, 180, 54),
            Triple(75, 190, 58),
            Triple(78, 190, 60)
        )

        weekCalories.forEachIndexed { index, value ->
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(6, 0, 6, 0)
                }
            }

            val proteinValue = macroMix[index].first
            val carbsValue = macroMix[index].second
            val fatValue = macroMix[index].third
            val totalMacro = proteinValue + carbsValue + fatValue
            val barHeight = ((value.toFloat() / maxValue.toFloat()) * 140f).toInt().coerceAtLeast(24)

            val barStack = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    barHeight.dp()
                )
            }

            val proteinHeight = ((proteinValue.toFloat() / totalMacro.toFloat()) * barHeight).toInt().coerceAtLeast(8)
            val carbsHeight = ((carbsValue.toFloat() / totalMacro.toFloat()) * barHeight).toInt().coerceAtLeast(8)
            val fatHeight = ((fatValue.toFloat() / totalMacro.toFloat()) * barHeight).toInt().coerceAtLeast(8)

            val proteinBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, proteinHeight.dp())
                background = ColorDrawable(Color.parseColor("#E53935"))
            }

            val carbsBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, carbsHeight.dp())
                background = ColorDrawable(Color.parseColor("#39A0ED"))
            }

            val fatBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fatHeight.dp())
                background = ColorDrawable(Color.parseColor("#FF9800"))
            }

            barStack.addView(fatBar)
            barStack.addView(carbsBar)
            barStack.addView(proteinBar)

            val label = TextView(this).apply {
                text = days[index]
                gravity = Gravity.CENTER
                setTextAppearance(android.R.style.TextAppearance_Material_Caption)
                setTextColor(if (isDarkTheme) Color.parseColor("#D1D5DB") else Color.parseColor("#697586"))
            }

            val valueText = TextView(this).apply {
                text = value.toString()
                gravity = Gravity.CENTER
                textSize = 11f
                setTextColor(if (isDarkTheme) Color.parseColor("#E5E7EB") else Color.parseColor("#1F2937"))
            }

            column.addView(valueText)
            column.addView(barStack)
            column.addView(label)
            chartContainer.addView(column)
        }
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}
