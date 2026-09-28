package com.example.caloriecounter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.SurfaceTexture
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Surface
import android.view.TextureView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Handler
import android.os.HandlerThread
import android.content.pm.PackageManager
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
    private var scanDetailsInputLayout: TextInputLayout? = null

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

        themeToggle.setOnClickListener {
            isDarkTheme = !isDarkTheme
            updateTheme()
            buildChart()
        }

        updateDashboard()
        updateTheme()
        buildChart()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showPage(Page.HOME)
                R.id.nav_scan -> showPage(Page.SCAN)
                R.id.nav_history -> showPage(Page.HISTORY)
                R.id.nav_settings -> showPage(Page.SETTINGS)
                else -> false
            }
        }
    }

    override fun onDestroy() {
        closeCamera()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            if (cameraTexture?.isAvailable == true) openCamera()
        }
    }

    private enum class Page { HOME, SCAN, HISTORY, SETTINGS }

    private fun showPage(page: Page): Boolean {
        closeCamera()
        scanDetailsInputLayout = null
        if (page == Page.HOME) {
            pageContainer.visibility = View.GONE
            homeContentViews.forEach { it.visibility = View.VISIBLE }
            titleText.text = getString(R.string.title_dashboard)
            return true
        }

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
            Page.HOME -> Unit
        }
        return true
    }

    private fun buildScanPage() {
        val intro = pageText(getString(R.string.scan_page_intro), 16f, true)
        pageContainer.addView(intro)

        cameraTexture = object : TextureView(this) {
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
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16.dp(), 0, 16.dp()) }
            isOpaque = true
            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) = openCamera()
                override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) = Unit
                override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean { closeCamera(); return true }
                override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) = Unit
            }
        }
        pageContainer.addView(cameraTexture)

        val scanButton = Button(this).apply {
            text = getString(R.string.scan_button)
            isAllCaps = false
            textSize = 18f
            minHeight = 64.dp()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                64.dp()
            ).apply { setMargins(0, 8.dp(), 0, 12.dp()) }
            setOnClickListener { }
        }
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
        listOf(
            "Dzisiaj, 12:40" to "Owsianka z owocami · 520 kcal",
            "Dzisiaj, 08:15" to "Jajka i pieczywo · 410 kcal",
            "Wczoraj, 19:05" to "Makaron z warzywami · 680 kcal"
        ).forEach { (time, meal) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
                background = ColorDrawable(if (isDarkTheme) Color.parseColor("#1F2937") else Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 12.dp(), 0, 0)
                }
            }
            row.addView(pageText(time, 12f, false))
            row.addView(pageText(meal, 16f, true))
            pageContainer.addView(row)
        }
    }

    private fun buildSettingsPage() {
        pageContainer.addView(pageText(getString(R.string.settings_page_intro), 16f, true))
        val notifications = Switch(this).apply {
            text = getString(R.string.settings_notifications)
            isChecked = true
            setTextColor(if (isDarkTheme) Color.WHITE else Color.parseColor("#1C2430"))
        }
        pageContainer.addView(notifications)
        val dailyLimit = Button(this).apply {
            text = getString(R.string.settings_daily_limit)
            setOnClickListener { }
        }
        pageContainer.addView(dailyLimit)
    }

    private fun pageText(text: String, size: Float, bold: Boolean): TextView = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
        val texture = cameraTexture ?: return
        if (!texture.isAvailable || cameraDevice != null || cameraThread != null) return

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = try {
            manager.cameraIdList.firstOrNull()
        } catch (_: Exception) {
            null
        } ?: return

        val previewSize = getPreviewSize(manager, cameraId, texture.width)
        val surfaceTexture = texture.surfaceTexture ?: return
        previewSize?.let { surfaceTexture.setDefaultBufferSize(it.width, it.height) }

        cameraThread = HandlerThread("camera").also { it.start() }
        cameraHandler = Handler(cameraThread!!.looper)
        try {
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    if (!texture.isAvailable || isFinishing || isDestroyed) {
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
                        camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                cameraSession = session
                                try {
                                    session.setRepeatingRequest(request.build(), null, cameraHandler)
                                } catch (_: Exception) {
                                    closeCamera()
                                }
                            }
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                closeCamera()
                            }
                        }, cameraHandler)
                    } catch (_: Exception) {
                        camera.close()
                        cameraDevice = null
                    }
                }
                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }
                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                }
            }, cameraHandler)
        } catch (_: Exception) {
            closeCamera()
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
