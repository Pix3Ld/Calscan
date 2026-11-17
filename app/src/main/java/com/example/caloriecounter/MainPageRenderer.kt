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
import com.example.caloriecounter.settings.CalorieProfile
import com.example.caloriecounter.settings.CalorieProfileSettingsController
import com.example.caloriecounter.settings.DailyLimitSettingsController
import com.example.caloriecounter.settings.SettingsPageRenderer
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

enum class Page { HOME, SCAN, HISTORY, ACTIVITY, ACTIVITY_DETAILS, SETTINGS, REMINDER_SETTINGS, LIMIT_SETTINGS, PROFILE_SETTINGS, DETAILS }

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
    private val profileSettingsController: CalorieProfileSettingsController,
    private val reminderController: MealReminderController,
    private val dailyLimitController: DailyLimitSettingsController,
    private val pageText: (String, Float, Boolean) -> TextView,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val isDarkTheme: () -> Boolean,
    private val applyTheme: () -> Unit,
    private val openSettingsDetail: (Page) -> Unit,
    private val onBack: () -> Unit,
    private val onLimitSummaryCreated: (TextView) -> Unit,
    private val onProfileSaved: (CalorieProfile, Boolean) -> Unit,
    private val requestCameraPermission: () -> Unit
) {
    private var historyContentContainer: LinearLayout? = null

    // Buduje zawartość żądanej strony; ekrany HOME i DETAILS są obsługiwane przez aktywność.
    fun render(
        page: Page,
        photos: List<PhotoHistoryEntity>,
        dailyLimit: Int,
        profile: CalorieProfile,
        smartCaloriesEnabled: Boolean,
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
                    profile = profile,
                    smartCaloriesEnabled = smartCaloriesEnabled,
                    onRemindersClick = { openSettingsDetail(Page.REMINDER_SETTINGS) },
                    onDailyLimitClick = { openSettingsDetail(Page.LIMIT_SETTINGS) },
                    onProfileClick = { openSettingsDetail(Page.PROFILE_SETTINGS) }
                )
            )
            Page.REMINDER_SETTINGS -> buildReminderSettingsPage()
            Page.LIMIT_SETTINGS -> buildDailyLimitPage(dailyLimit, smartCaloriesEnabled)
            Page.PROFILE_SETTINGS -> buildProfileSettingsPage(profile, smartCaloriesEnabled)
            Page.HOME, Page.ACTIVITY_DETAILS, Page.DETAILS -> Unit
        }
    }

    fun updateActivity(snapshot: ActivitySnapshot, sessions: List<ActivitySessionEntity>) {
        activityTrackingRenderer.update(snapshot, sessions)
    }

    fun renderPhotoHistory(photos: List<PhotoHistoryEntity>) {
        historyContentContainer?.let { historyRenderer.render(it, photos) }
    }

    private val activeInputLayoutsList = mutableListOf<TextInputLayout>()

    fun clearDynamicFields() {
        activeInputLayoutsList.clear()
    }

    fun activeInputLayouts(): List<TextInputLayout> = activeInputLayoutsList.toList()

    private fun buildScanPage() {
        activeInputLayoutsList.add(scanRenderer.render(pageContainer))
        applyTheme()
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }
    }

    private fun buildHistoryPage(photos: List<PhotoHistoryEntity>) {
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

    private fun buildDailyLimitPage(dailyLimit: Int, smartCaloriesEnabled: Boolean) {
        addSettingsBackButton()
        if (smartCaloriesEnabled) {
            pageContainer.addView(pageText(
                activity.getString(R.string.daily_limit_automatic_description, dailyLimit),
                15f,
                false
            ))
        } else {
            activeInputLayoutsList.add(dailyLimitController.buildPage(pageContainer, dailyLimit))
        }
        applyTheme()
    }

    private fun buildProfileSettingsPage(profile: CalorieProfile, smartCaloriesEnabled: Boolean) {
        addSettingsBackButton()
        val scrollView = ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(content)
        pageContainer.addView(scrollView)
        activeInputLayoutsList.addAll(
            profileSettingsController.buildPage(
                container = content,
                profile = profile,
                smartCaloriesEnabled = smartCaloriesEnabled,
                onSave = onProfileSaved
            )
        )
        applyTheme()
    }

    private fun addSettingsBackButton() {
        pageContainer.addView(styledButton(activity.getString(R.string.settings_back), false).apply {
            setIconResource(R.drawable.ic_arrow_back_24)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconPadding = 8.dp()
            iconTint = android.content.res.ColorStateList.valueOf(
                if (isDarkTheme()) android.graphics.Color.parseColor("#F3F4F6")
                else android.graphics.Color.parseColor("#1E654C")
            )
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                40.dp()
            ).apply {
                setMargins(0, 0, 0, 8.dp())
            }
            setOnClickListener { onBack() }
        })
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
