package com.example.caloriecounter

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.camera.ScanPageRenderer
import com.example.caloriecounter.activity.ActivitySnapshot
import com.example.caloriecounter.activity.ActivityTrackingRenderer
import com.example.caloriecounter.data.ActivitySessionEntity
import com.example.caloriecounter.data.PhotoHistoryEntity
import com.example.caloriecounter.history.MealHistoryRenderer
import com.example.caloriecounter.reminders.MealReminderController
import com.example.caloriecounter.settings.DailyLimitSettingsController
import com.example.caloriecounter.settings.SettingsPageRenderer
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

enum class Page { HOME, SCAN, HISTORY, ACTIVITY, SETTINGS, REMINDER_SETTINGS, LIMIT_SETTINGS, DETAILS }

/**
 * Tworzy zawartość ekranów głównych i deleguje wyspecjalizowane sekcje do rendererów.
 */
class MainPageRenderer(
    private val activity: AppCompatActivity,
    private val pageContainer: LinearLayout,
    private val scanRenderer: ScanPageRenderer,
    private val historyRenderer: MealHistoryRenderer,
    private val activityTrackingRenderer: ActivityTrackingRenderer,
    private val settingsRenderer: SettingsPageRenderer,
    private val reminderController: MealReminderController,
    private val dailyLimitController: DailyLimitSettingsController,
    private val pageText: (String, Float, Boolean) -> TextView,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val isDarkTheme: () -> Boolean,
    private val applyTheme: () -> Unit,
    private val openSettingsDetail: (Page) -> Unit,
    private val onBack: () -> Unit,
    private val onLimitSummaryCreated: (TextView) -> Unit,
    private val requestCameraPermission: () -> Unit
) {
    private var historyContentContainer: LinearLayout? = null
    private var activeInputLayout: TextInputLayout? = null

    // Buduje zawartość żądanej strony; ekrany HOME i DETAILS są obsługiwane przez aktywność.
    fun render(
        page: Page,
        photos: List<PhotoHistoryEntity>,
        dailyLimit: Int,
        activitySnapshot: ActivitySnapshot,
        activitySessions: List<ActivitySessionEntity>
    ) {
        when (page) {
            Page.SCAN -> buildScanPage()
            Page.HISTORY -> buildHistoryPage(photos)
            Page.ACTIVITY -> activityTrackingRenderer.render(activitySnapshot, activitySessions)
            Page.SETTINGS -> onLimitSummaryCreated(
                settingsRenderer.render(
                    container = pageContainer,
                    dailyLimit = dailyLimit,
                    onRemindersClick = { openSettingsDetail(Page.REMINDER_SETTINGS) },
                    onDailyLimitClick = { openSettingsDetail(Page.LIMIT_SETTINGS) }
                )
            )
            Page.REMINDER_SETTINGS -> buildReminderSettingsPage()
            Page.LIMIT_SETTINGS -> buildDailyLimitPage(dailyLimit)
            Page.HOME, Page.DETAILS -> Unit
        }
    }

    fun updateActivity(snapshot: ActivitySnapshot, sessions: List<ActivitySessionEntity>) {
        activityTrackingRenderer.update(snapshot, sessions)
    }

    fun renderPhotoHistory(photos: List<PhotoHistoryEntity>) {
        historyContentContainer?.let { historyRenderer.render(it, photos) }
    }

    fun clearDynamicFields() {
        activeInputLayout = null
    }

    fun currentInputLayout(): TextInputLayout? = activeInputLayout

    private fun buildScanPage() {
        activeInputLayout = scanRenderer.render(pageContainer)
        applyTheme()
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }
    }

    private fun buildHistoryPage(photos: List<PhotoHistoryEntity>) {
        pageContainer.addView(pageText(activity.getString(R.string.history_page_intro), 16f, true))
        val scrollView = ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        historyContentContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(historyContentContainer)
        pageContainer.addView(scrollView)
        renderPhotoHistory(photos)
    }

    private fun buildReminderSettingsPage() {
        addSettingsBackButton()
        reminderController.buildSettingsPage(pageContainer)
    }

    private fun buildDailyLimitPage(dailyLimit: Int) {
        addSettingsBackButton()
        activeInputLayout = dailyLimitController.buildPage(pageContainer, dailyLimit)
        applyTheme()
    }

    private fun addSettingsBackButton() {
        pageContainer.addView(styledButton(activity.getString(R.string.settings_back), false).apply {
            setIconResource(R.drawable.ic_arrow_back_24)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconPadding = 8.dp()
            iconTint = android.content.res.ColorStateList.valueOf(
                if (isDarkTheme()) android.graphics.Color.parseColor("#D7F3E3")
                else android.graphics.Color.parseColor("#1E654C")
            )
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                48.dp()
            ).apply {
                setMargins(0, 0, 0, 8.dp())
            }
            setOnClickListener { onBack() }
        })
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
