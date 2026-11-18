package com.example.caloriecounter.settings

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

/** Buduje formularz profilu używany przy aktywności i wyliczaniu limitu kalorii. */
class CalorieProfileSettingsController(
    private val activity: AppCompatActivity,
    private val pageText: (String, Float, Boolean) -> TextView,
    private val styledButton: (String, Boolean) -> MaterialButton,
    private val isDarkTheme: () -> Boolean
) {
    fun buildPage(
        container: LinearLayout,
        profile: CalorieProfile,
        smartCaloriesEnabled: Boolean,
        onSave: (CalorieProfile, Boolean) -> Unit
    ): List<TextInputLayout> {
        var useSmartCalories = smartCaloriesEnabled
        val smartSummary = pageText(activity.getString(smartModeSummary(useSmartCalories)), 14f, false).apply {
            setTextColor(secondaryTextColor())
            setPadding(0, 4.dp(), 0, 0)
        }
        val smartSwitch = SwitchMaterial(activity).apply {
            isChecked = useSmartCalories
            contentDescription = activity.getString(R.string.settings_smart_calories_title)
            val checkedColor = if (isDarkTheme()) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C")
            val uncheckedThumb = if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586")
            val checkedTrack = if (isDarkTheme()) Color.parseColor("#527565") else Color.parseColor("#B8D6C4")
            val uncheckedTrack = if (isDarkTheme()) Color.parseColor("#44504A") else Color.parseColor("#D8E0DA")
            val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
            thumbTintList = ColorStateList(states, intArrayOf(checkedColor, uncheckedThumb))
            trackTintList = ColorStateList(states, intArrayOf(checkedTrack, uncheckedTrack))
            setOnCheckedChangeListener { _, checked ->
                useSmartCalories = checked
                smartSummary.text = activity.getString(smartModeSummary(checked))
            }
        }
        container.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16.dp(), 12.dp(), 12.dp(), 12.dp())
            background = surfaceBackground()
            addView(LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                addView(pageText(activity.getString(R.string.settings_smart_calories_title), 16f, true))
                addView(smartSummary)
            })
            addView(smartSwitch)
        }, fieldLayoutParams())

        val activityLevels = listOf(
            R.string.profile_activity_sedentary,
            R.string.profile_activity_light,
            R.string.profile_activity_moderate,
            R.string.profile_activity_high,
            R.string.profile_activity_very_high
        ).map { activity.getString(it) }
        var selectedActivityLevel = profile.activityLevel.coerceIn(0, activityLevels.lastIndex)

        val activityLevelLayout = TextInputLayout(activity).apply {
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxBackgroundColor(fieldBackgroundColor())
            boxStrokeColor = fieldStrokeColor()
            boxStrokeWidth = 1.dp()
            boxStrokeWidthFocused = 2.dp()
            defaultHintTextColor = ColorStateList.valueOf(secondaryTextColor())
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            setEndIconTintList(ColorStateList.valueOf(secondaryTextColor()))
            layoutParams = LinearLayout.LayoutParams(
                165.dp(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val dropdownAdapter = object : ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, activityLevels) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as? TextView)?.apply {
                    setTextColor(primaryTextColor())
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f)
                    setPadding(16.dp(), 12.dp(), 16.dp(), 12.dp())
                }
                return v
            }
        }

        activityLevelLayout.addView(MaterialAutoCompleteTextView(activity).apply {
            inputType = InputType.TYPE_NULL
            setAdapter(dropdownAdapter)
            setText(activityLevels[selectedActivityLevel], false)
            setTextColor(primaryTextColor())
            setTextSize(14f)
            setPadding(10.dp(), 12.dp(), 6.dp(), 12.dp())
            background = null
            setDropDownBackgroundDrawable(surfaceBackground())
            keyListener = null
            isFocusable = false
            setOnClickListener { showDropDown() }
            setOnItemClickListener { _, _, position, _ -> selectedActivityLevel = position }
        })

        val activityCard = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16.dp(), 10.dp(), 12.dp(), 10.dp())
            background = surfaceBackground()
            addView(LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = 8.dp()
                }
                addView(pageText(activity.getString(R.string.profile_activity_label), 16f, true))
            })
            addView(activityLevelLayout)
        }
        container.addView(activityCard, fieldLayoutParams())

        val weight = buildNumberField(
            R.string.profile_weight_label,
            profile.weightKg.takeIf { it > 0f }?.toString()?.removeSuffix(".0") ?: "",
            decimal = true
        )
        val height = buildNumberField(
            R.string.profile_height_label,
            profile.heightCm.takeIf { it > 0 }?.toString() ?: "",
            maxLength = 3
        )
        val age = buildNumberField(
            R.string.profile_age_label,
            profile.ageYears.takeIf { it > 0 }?.toString() ?: "",
            maxLength = 3
        )
        listOf(weight, height, age).forEach { container.addView(it.first, fieldLayoutParams()) }

        container.addView(pageText(activity.getString(R.string.profile_sex_label), 15f, true).apply {
            setPadding(0, 18.dp(), 0, 4.dp())
        })
        val sexGroup = RadioGroup(activity).apply {
            orientation = RadioGroup.HORIZONTAL
            addView(profileRadio(R.string.profile_sex_female, MaintenanceCalorieCalculator.SEX_FEMALE))
            addView(profileRadio(R.string.profile_sex_male, MaintenanceCalorieCalculator.SEX_MALE))
        }
        val sexError = pageText(activity.getString(R.string.profile_sex_invalid), 13f, false).apply {
            setTextColor(Color.parseColor("#D14343"))
            visibility = View.GONE
            setPadding(0, 2.dp(), 0, 0)
        }
        sexGroup.check(
            when (profile.sex) {
                MaintenanceCalorieCalculator.SEX_FEMALE -> sexGroup.getChildAt(0).id
                MaintenanceCalorieCalculator.SEX_MALE -> sexGroup.getChildAt(1).id
                else -> View.NO_ID
            }
        )
        sexGroup.setOnCheckedChangeListener { _, _ -> sexError.visibility = View.GONE }
        container.addView(sexGroup)
        container.addView(sexError)

        container.addView(styledButton(activity.getString(R.string.profile_save), true).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                48.dp()
            ).apply { topMargin = 20.dp() }
            setOnClickListener {
                val weightKg = weight.second.text?.toString()?.replace(',', '.')?.toFloatOrNull()
                val heightCm = height.second.text?.toString()?.toIntOrNull()
                val ageYears = age.second.text?.toString()?.toIntOrNull()
                val selectedSex = sexGroup.findViewById<RadioButton>(sexGroup.checkedRadioButtonId)
                    ?.tag as? String

                weight.first.error = if (weightKg == null || weightKg !in 20f..300f) {
                    activity.getString(R.string.profile_weight_invalid)
                } else {
                    null
                }
                height.first.error = when {
                    useSmartCalories && (heightCm == null || heightCm !in 100..250) ->
                        activity.getString(R.string.profile_height_invalid)
                    heightCm != null && heightCm !in 100..250 ->
                        activity.getString(R.string.profile_height_invalid)
                    else -> null
                }
                age.first.error = when {
                    useSmartCalories && (ageYears == null || ageYears !in 13..120) ->
                        activity.getString(R.string.profile_age_invalid)
                    ageYears != null && ageYears !in 13..120 ->
                        activity.getString(R.string.profile_age_invalid)
                    else -> null
                }
                sexError.visibility = if (useSmartCalories && selectedSex == null) View.VISIBLE else View.GONE

                if (weight.first.error != null || height.first.error != null ||
                    age.first.error != null || sexError.visibility == View.VISIBLE
                ) {
                    return@setOnClickListener
                }

                onSave(
                    CalorieProfile(
                        weightKg = weightKg ?: return@setOnClickListener,
                        heightCm = heightCm ?: 0,
                        ageYears = ageYears ?: 0,
                        sex = selectedSex.orEmpty(),
                        activityLevel = selectedActivityLevel
                    ),
                    useSmartCalories
                )
            }
        })

        return listOf(weight.first, height.first, age.first, activityLevelLayout)
    }

    private fun buildNumberField(
        label: Int,
        value: String,
        decimal: Boolean = false,
        maxLength: Int? = null,
        topPadding: Int = 16,
        bottomPadding: Int = 16
    ): Pair<TextInputLayout, TextInputEditText> {
        val layout = TextInputLayout(activity).apply {
            hint = activity.getString(label)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxBackgroundColor(fieldBackgroundColor())
            boxStrokeColor = fieldStrokeColor()
            boxStrokeWidth = 1.dp()
            boxStrokeWidthFocused = 2.dp()
            defaultHintTextColor = ColorStateList.valueOf(secondaryTextColor())
            layoutParams = fieldLayoutParams()
        }
        val input = TextInputEditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or
                (if (decimal) InputType.TYPE_NUMBER_FLAG_DECIMAL else 0)
            maxLines = 1
            maxLength?.let { filters = arrayOf(InputFilter.LengthFilter(it)) }
            setText(value)
            setTextSize(16f)
            setPadding(12.dp(), topPadding.dp(), 12.dp(), bottomPadding.dp())
            setTextColor(primaryTextColor())
            setHintTextColor(secondaryTextColor())
        }
        layout.addView(input)
        return layout to input
    }

    private fun profileRadio(label: Int, value: String): RadioButton =
        RadioButton(activity).apply {
            id = View.generateViewId()
            text = activity.getString(label)
            tag = value
            setTextColor(primaryTextColor())
            buttonTintList = ColorStateList.valueOf(
                if (isDarkTheme()) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C")
            )
        }

    private fun smartModeSummary(enabled: Boolean): Int =
        if (enabled) R.string.settings_smart_calories_on else R.string.settings_smart_calories_off

    private fun fieldLayoutParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = 10.dp() }

    private fun surfaceBackground() = GradientDrawable().apply {
        cornerRadius = 12.dp().toFloat()
        setColor(if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE)
        setStroke(1.dp(), fieldStrokeColor())
    }

    private fun fieldBackgroundColor(): Int =
        if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE

    private fun fieldStrokeColor(): Int =
        if (isDarkTheme()) Color.parseColor("#52637A") else Color.parseColor("#B8D6C4")

    private fun primaryTextColor(): Int =
        if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")

    private fun secondaryTextColor(): Int =
        if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586")

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
