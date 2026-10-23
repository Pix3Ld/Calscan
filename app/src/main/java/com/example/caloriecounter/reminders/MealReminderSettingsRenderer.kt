package com.example.caloriecounter.reminders

import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.google.android.material.button.MaterialButton

data class ReminderSettingsViews(
    val toggle: Switch,
    val options: LinearLayout
)

/**
 * Buduje ekran ustawień przypomnień oraz przyciski liczby i godzin posiłków.
 */
class MealReminderSettingsRenderer(
    private val activity: AppCompatActivity,
    private val settingsStore: MealReminderSettingsStore,
    private val isDarkTheme: () -> Boolean,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val onToggleChanged: (Switch, Boolean) -> Unit,
    private val onMealCountSelected: (Int?) -> Unit,
    private val onMealTimesChanged: (MealReminderSettings) -> Unit
) {
    // Tworzy przełącznik i kontener opcji; obsługa uprawnień pozostaje w aktywności.
    fun buildPage(pageContainer: LinearLayout): ReminderSettingsViews {
        val scrollView = ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8.dp(), 0, 16.dp())
        }
        scrollView.addView(content)
        pageContainer.addView(scrollView)
        content.addView(pageText(activity.getString(R.string.settings_reminders_intro), 15f, false))

        val settings = settingsStore.load()
        val reminderSwitch = Switch(activity).apply {
            text = activity.getString(R.string.settings_notifications)
            isChecked = settings.enabled
            setTextColor(if (isDarkTheme()) Color.WHITE else Color.parseColor("#1C2430"))
            val checkedColor = Color.parseColor("#1E654C")
            val uncheckedColor = if (isDarkTheme()) Color.parseColor("#788579") else Color.parseColor("#AAB7AE")
            val tintStates = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
            thumbTintList = ColorStateList(tintStates, intArrayOf(checkedColor, uncheckedColor))
            trackTintList = ColorStateList(
                tintStates,
                intArrayOf(
                    if (isDarkTheme()) Color.parseColor("#527565") else Color.parseColor("#B8D6C4"),
                    if (isDarkTheme()) Color.parseColor("#44504A") else Color.parseColor("#D8E0DA")
                )
            )
        }
        reminderSwitch.setOnCheckedChangeListener { _, checked ->
            onToggleChanged(reminderSwitch, checked)
        }
        content.addView(reminderSwitch)

        val options = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            visibility = if (settings.enabled) View.VISIBLE else View.GONE
        }
        content.addView(options)
        return ReminderSettingsViews(reminderSwitch, options)
    }

    // Odświeża przyciski liczby posiłków i godzin na podstawie zapisanych ustawień.
    fun renderOptions(container: LinearLayout) {
        val settings = settingsStore.load()
        container.removeAllViews()
        container.addView(styledButton(
            activity.getString(R.string.reminder_meal_count_label, settings.mealCount),
            false
        ).apply {
            setOnClickListener { onMealCountSelected(settings.mealCount) }
        }, reminderButtonLayoutParams())

        settings.mealTimesMinutes.forEachIndexed { index, minutes ->
            container.addView(styledButton(
                activity.getString(
                    R.string.reminder_meal_time_label,
                    index + 1,
                    formatMealTime(minutes)
                ),
                false
            ).apply {
                setOnClickListener {
                    TimePickerDialog(
                        activity,
                        { _, hour, minute ->
                            val updatedTimes = settings.mealTimesMinutes.toMutableList()
                            updatedTimes[index] = hour * 60 + minute
                            onMealTimesChanged(settings.copy(mealTimesMinutes = updatedTimes))
                        },
                        minutes / 60,
                        minutes % 60,
                        true
                    ).show()
                }
            }, reminderButtonLayoutParams())
        }
    }

    private fun reminderButtonLayoutParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        56.dp()
    ).apply { setMargins(0, 8.dp(), 0, 0) }

    private fun formatMealTime(minutes: Int): String =
        String.format(java.util.Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)

    private fun pageText(text: String, size: Float, bold: Boolean): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = size
            setTextColor(if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
