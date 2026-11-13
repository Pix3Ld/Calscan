package com.example.caloriecounter.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.caloriecounter.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Zarządza stanem przypomnień, wymaganymi uprawnieniami i ekranem ustawień posiłków.
 */
class MealReminderController(
    private val activity: AppCompatActivity,
    private val settingsStore: MealReminderSettingsStore,
    private val isDarkTheme: () -> Boolean,
    private val isHomePage: () -> Boolean,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val requestNotificationPermission: () -> Unit,
    private val requestExactAlarmAccess: () -> Unit,
    private val openSettings: () -> Unit
) {
    private var reminderSwitch: Switch? = null
    private var reminderOptions: LinearLayout? = null
    private var noticeView: TextView? = null
    private var updatingSwitch = false
    private var pendingEnable = false
    val homeNotice: TextView?
        get() = noticeView

    private val renderer by lazy {
        MealReminderSettingsRenderer(
            activity = activity,
            settingsStore = settingsStore,
            isDarkTheme = isDarkTheme,
            styledButton = styledButton,
            onToggleChanged = ::onToggleChanged,
            onMealCountSelected = ::showMealCountPicker,
            onMealTimesChanged = ::saveSettings
        )
    }

    // Dodaje na ekran główny komunikat prowadzący do ustawień przypomnień.
    fun attachHomeNotice(root: LinearLayout) {
        noticeView = TextView(activity).apply {
            text = activity.getString(R.string.home_reminder_notice)
            textSize = 14f
            setTextColor(Color.parseColor("#1C5DD9"))
            setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
            background = GradientDrawable().apply {
                cornerRadius = 8.dp().toFloat()
                setColor(Color.parseColor("#E6F0FF"))
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { openSettings() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16.dp(), 12.dp(), 16.dp(), 0) }
        }
        root.addView(noticeView, 2)
        updateHomeNotice()
    }

    // Tworzy ustawienia przypomnień i zachowuje widoki do późniejszego odświeżania.
    fun buildSettingsPage(container: LinearLayout) {
        val views = renderer.buildPage(container)
        reminderSwitch = views.toggle
        reminderOptions = views.options
        renderOptions()
    }

    // Synchronizuje ustawienia alarmów z uprawnieniami po każdym powrocie aplikacji na ekran.
    fun onResume() {
        if (!pendingEnable) {
            val settings = settingsStore.load()
            if (settings.enabled) {
                if (!reminderPermissionsGranted()) {
                    settingsStore.save(settings.copy(enabled = false))
                    MealReminderScheduler.cancelAll(activity)
                    setSwitchChecked(false)
                    reminderOptions?.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        R.string.reminder_permissions_required,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    MealReminderScheduler.scheduleAll(activity)
                }
            }
        }
        updateHomeNotice()
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) continueEnableFlow() else rejectEnable()
    }

    fun onExactAlarmSettingsResult() {
        if (!pendingEnable) return
        if (MealReminderScheduler.hasExactAlarmAccess(activity)) {
            continueEnableFlow()
        } else {
            rejectEnable()
        }
    }

    fun onPageChanged() {
        updateHomeNotice()
    }

    private fun onToggleChanged(@Suppress("UNUSED_PARAMETER") toggle: Switch, checked: Boolean) {
        if (updatingSwitch) return
        if (checked) {
            setSwitchChecked(false)
            pendingEnable = true
            continueEnableFlow()
        } else {
            disableReminders()
        }
    }

    private fun continueEnableFlow() {
        if (!pendingEnable) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission()
            return
        }
        if (!MealReminderScheduler.hasExactAlarmAccess(activity)) {
            requestExactAlarmAccess()
            return
        }

        pendingEnable = false
        val settings = settingsStore.load()
        if (settings.hasConfiguredMeals) {
            enableReminders(settings)
        } else {
            showMealCountOptIn()
        }
    }

    private fun showMealCountOptIn() {
        val dialogTheme = if (isDarkTheme()) R.style.AppTheme_Dialog_Dark else R.style.AppTheme_Dialog_Light
        MaterialAlertDialogBuilder(activity, dialogTheme)
            .setTitle(R.string.reminder_choose_meal_count_title)
            .setMessage(R.string.reminder_choose_meal_count_message)
            .setPositiveButton(R.string.reminder_choose_meal_count) { _, _ -> showMealCountPicker(null) }
            .setNegativeButton(R.string.reminder_keep_default) { _, _ ->
                val settings = settingsStore.load()
                enableReminders(
                    settings.copy(
                        mealTimesMinutes = ReminderTimeCalculator.defaultTimes(settings.mealCount),
                        hasConfiguredMeals = true
                    )
                )
            }
            .setOnCancelListener { setSwitchChecked(false) }
            .show()
    }

    private fun showMealCountPicker(currentCount: Int?) {
        val isDark = isDarkTheme()
        val dialogTheme = if (isDark) R.style.AppTheme_Dialog_Dark else R.style.AppTheme_Dialog_Light

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp(), 16.dp(), 24.dp(), 8.dp())
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }

        val picker = android.widget.NumberPicker(activity).apply {
            minValue = ReminderTimeCalculator.MIN_MEALS
            maxValue = ReminderTimeCalculator.MAX_MEALS
            value = currentCount ?: ReminderTimeCalculator.DEFAULT_MEALS
            wrapSelectorWheel = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        styleNumberPicker(picker, isDark)
        container.addView(picker)

        MaterialAlertDialogBuilder(activity, dialogTheme)
            .setTitle(R.string.reminder_meal_count_title)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val oldSettings = settingsStore.load()
                val newSettings = oldSettings.copy(
                    mealCount = picker.value,
                    mealTimesMinutes = ReminderTimeCalculator.defaultTimes(picker.value),
                    hasConfiguredMeals = true
                )
                if (oldSettings.enabled) saveSettings(newSettings) else enableReminders(newSettings)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                if (currentCount == null) setSwitchChecked(false)
            }
            .setOnCancelListener {
                if (currentCount == null) setSwitchChecked(false)
            }
            .show()
    }

    private fun styleNumberPicker(picker: android.widget.NumberPicker, isDark: Boolean) {
        val textColor = if (isDark) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            picker.textColor = textColor
        }
    }

    private fun enableReminders(settings: MealReminderSettings) {
        if (!reminderPermissionsGranted()) {
            setSwitchChecked(false)
            Toast.makeText(
                activity,
                R.string.reminder_permissions_required,
                Toast.LENGTH_LONG
            ).show()
            return
        }
        saveSettings(settings.copy(enabled = true, hasConfiguredMeals = true))
    }

    private fun disableReminders() {
        pendingEnable = false
        settingsStore.save(settingsStore.load().copy(enabled = false))
        MealReminderScheduler.cancelAll(activity)
        reminderOptions?.visibility = View.GONE
        updateHomeNotice()
    }

    private fun saveSettings(settings: MealReminderSettings) {
        settingsStore.save(settings)
        if (settings.enabled) {
            MealReminderScheduler.scheduleAll(activity)
        } else {
            MealReminderScheduler.cancelAll(activity)
        }
        setSwitchChecked(settings.enabled)
        reminderOptions?.visibility = if (settings.enabled) View.VISIBLE else View.GONE
        renderOptions()
        updateHomeNotice()
    }

    private fun renderOptions() {
        reminderOptions?.let(renderer::renderOptions)
    }

    private fun setSwitchChecked(checked: Boolean) {
        updatingSwitch = true
        reminderSwitch?.isChecked = checked
        updatingSwitch = false
    }

    private fun reminderPermissionsGranted(): Boolean {
        val notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return notificationsGranted && MealReminderScheduler.hasExactAlarmAccess(activity)
    }

    private fun rejectEnable() {
        pendingEnable = false
        setSwitchChecked(false)
        Toast.makeText(
            activity,
            R.string.reminder_permissions_required,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateHomeNotice() {
        noticeView?.visibility = if (
            isHomePage() && !settingsStore.load().enabled
        ) View.VISIBLE else View.GONE
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
