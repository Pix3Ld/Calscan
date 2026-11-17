package com.example.caloriecounter.reminders

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

data class ReminderSettingsViews(
    val toggle: Switch,
    val options: LinearLayout
)

/**
 * Buduje ekran ustawień przypomnień oraz pola liczby i godzin posiłków.
 */
class MealReminderSettingsRenderer(
    private val activity: AppCompatActivity,
    private val settingsStore: MealReminderSettingsStore,
    private val isDarkTheme: () -> Boolean,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val onToggleChanged: (Switch, Boolean) -> Unit,
    private val onMealCountSelected: (Int) -> Unit,
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

    // Odświeża liczbę i godziny posiłków na podstawie zapisanych ustawień.
    fun renderOptions(container: LinearLayout) {
        val settings = settingsStore.load()
        container.removeAllViews()
        val mealCountField = createMealCountDropdown(settings.mealCount, onMealCountSelected).first
        container.addView(
            mealCountField,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8.dp(), 0, 0) }
        )

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
                    val timePickerTheme = if (isDarkTheme()) {
                        R.style.AppTheme_TimePicker_Dark
                    } else {
                        R.style.AppTheme_TimePicker_Light
                    }
                    val picker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(minutes / 60)
                        .setMinute(minutes % 60)
                        .setTitleText(activity.getString(R.string.reminder_meal_time_title))
                        .setTheme(timePickerTheme)
                        .build()

                    picker.addOnPositiveButtonClickListener {
                        val updatedTimes = settings.mealTimesMinutes.toMutableList()
                        updatedTimes[index] = picker.hour * 60 + picker.minute
                        onMealTimesChanged(settings.copy(mealTimesMinutes = updatedTimes))
                    }

                    picker.show(activity.supportFragmentManager, "time_picker_$index")
                }
            }, reminderButtonLayoutParams())
        }
    }

    fun createMealCountDropdown(
        initialCount: Int,
        onSelection: ((Int) -> Unit)? = null
    ): Pair<TextInputLayout, () -> Int> {
        var selectedCount = initialCount.coerceIn(
            ReminderTimeCalculator.MIN_MEALS,
            ReminderTimeCalculator.MAX_MEALS
        )
        val fieldBackground = if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE
        val textColor = if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
        val fieldHint = if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#58677A")
        val fieldStroke = if (isDarkTheme()) Color.parseColor("#52637A") else Color.parseColor("#B8D6C4")

        val layout = TextInputLayout(activity).apply {
            hint = activity.getString(R.string.reminder_meal_count_title)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxBackgroundColor(fieldBackground)
            boxStrokeColor = fieldStroke
            boxStrokeWidth = 1.dp()
            boxStrokeWidthFocused = 2.dp()
            defaultHintTextColor = ColorStateList.valueOf(fieldHint)
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            setEndIconTintList(ColorStateList.valueOf(fieldHint))
        }

        val mealCounts = (ReminderTimeCalculator.MIN_MEALS..ReminderTimeCalculator.MAX_MEALS)
            .map(Int::toString)

        val dropdownAdapter = object : ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item, mealCounts) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as? TextView)?.apply {
                    setTextColor(textColor)
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                }
                return v
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as? TextView)?.apply {
                    setTextColor(textColor)
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f)
                    setPadding(16.dp(), 12.dp(), 16.dp(), 12.dp())
                }
                return v
            }
        }

        layout.addView(MaterialAutoCompleteTextView(activity).apply {
            setAdapter(dropdownAdapter)
            setText(selectedCount.toString(), false)
            setTextColor(textColor)
            setTextSize(16f)
            setPadding(12.dp(), 16.dp(), 12.dp(), 16.dp())
            background = null
            setDropDownBackgroundDrawable(android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 12.dp().toFloat()
                setColor(fieldBackground)
                setStroke(1.dp(), fieldStroke)
            })
            keyListener = null
            isFocusable = false
            setOnItemClickListener { _, _, position, _ ->
                selectedCount = ReminderTimeCalculator.MIN_MEALS + position
                onSelection?.invoke(selectedCount)
            }
        })
        return layout to { selectedCount }
    }

    private fun reminderButtonLayoutParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        48.dp()
    ).apply { setMargins(0, 8.dp(), 0, 0) }

    private fun formatMealTime(minutes: Int): String =
        String.format(java.util.Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
