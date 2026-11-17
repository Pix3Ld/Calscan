package com.example.caloriecounter.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.caloriecounter.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout

/**
 * Stosuje wybrany motyw do stałych elementów ekranu głównego i aktualnie otwartych formularzy.
 */
class AppThemeApplier(private val activity: AppCompatActivity) {
    fun apply(
        isDarkTheme: Boolean,
        reminderHomeNotice: TextView?,
        inputLayouts: List<TextInputLayout?>
    ) {
        val backgroundColor = if (isDarkTheme) Color.parseColor("#121826") else Color.parseColor("#F5F7FA")
        val cardColor = if (isDarkTheme) Color.parseColor("#1F2937") else Color.WHITE
        val primaryTextColor = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
        val secondaryTextColor = if (isDarkTheme) Color.parseColor("#D1D5DB") else Color.parseColor("#697586")
        val buttonBackground = if (isDarkTheme) Color.parseColor("#2F3B52") else Color.parseColor("#E5E7EB")
        val buttonIconColor = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#111827")
        val navBg = cardColor
        val navItemColor = if (isDarkTheme) Color.parseColor("#D1D5DB") else Color.parseColor("#697586")
        val selectedNavItemColor = if (isDarkTheme) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C")

        activity.window.statusBarColor = backgroundColor
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkTheme
        }

        activity.findViewById<LinearLayout>(R.id.rootLayout).setBackgroundColor(backgroundColor)
        activity.findViewById<TextView>(R.id.titleText).setTextColor(primaryTextColor)
        activity.findViewById<TextView>(R.id.chartTitle).setTextColor(primaryTextColor)

        activity.findViewById<ImageButton>(R.id.themeToggle).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12.dp().toFloat()
                setColor(buttonBackground)
            }
            elevation = 0f
            setColorFilter(buttonIconColor)
            setImageResource(if (isDarkTheme) R.drawable.ic_moon_24 else R.drawable.ic_sun_24)
        }

        listOf(R.id.todayCard, R.id.remainingCard, R.id.yesterdayCard, R.id.weekCard, R.id.chartCard)
            .forEach { id ->
                activity.findViewById<View>(id).background = GradientDrawable().apply {
                    cornerRadius = 12.dp().toFloat()
                    setColor(cardColor)
                }
            }
        activity.findViewById<BottomNavigationView>(R.id.bottomNavigation).apply {
            setBackgroundColor(navBg)
            val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
            val colors = intArrayOf(selectedNavItemColor, navItemColor)
            itemIconTintList = ColorStateList(states, colors)
            itemTextColor = ColorStateList(states, colors)
        }

        activity.findViewById<TextView>(R.id.todayValue).setTextColor(primaryTextColor)
        activity.findViewById<TextView>(R.id.yesterdayValue).setTextColor(primaryTextColor)
        activity.findViewById<TextView>(R.id.weekValue).setTextColor(primaryTextColor)
        listOf(R.id.labelToday, R.id.labelRemaining, R.id.labelYesterday, R.id.labelWeek)
            .forEach { id -> activity.findViewById<TextView>(id).setTextColor(secondaryTextColor) }

        val macroTextColor = if (isDarkTheme) Color.parseColor("#E5E7EB") else Color.parseColor("#1F2937")
        listOf(R.id.proteinLabel, R.id.carbsLabel, R.id.fatLabel)
            .forEach { id -> activity.findViewById<TextView>(id).setTextColor(macroTextColor) }

        val proteinColor = if (isDarkTheme) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C")
        val carbsColor = if (isDarkTheme) Color.parseColor("#F87171") else Color.parseColor("#DC2626")
        val fatColor = if (isDarkTheme) Color.parseColor("#FBBF24") else Color.parseColor("#D97706")

        activity.findViewById<View>(R.id.proteinIndicator)?.background = GradientDrawable().apply {
            cornerRadius = 2.dp().toFloat()
            setColor(proteinColor)
        }
        activity.findViewById<View>(R.id.carbsIndicator)?.background = GradientDrawable().apply {
            cornerRadius = 2.dp().toFloat()
            setColor(carbsColor)
        }
        activity.findViewById<View>(R.id.fatIndicator)?.background = GradientDrawable().apply {
            cornerRadius = 2.dp().toFloat()
            setColor(fatColor)
        }

        reminderHomeNotice?.apply {
            setTextColor(if (isDarkTheme) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C"))
            background = GradientDrawable().apply {
                cornerRadius = 12.dp().toFloat()
                setColor(if (isDarkTheme) Color.parseColor("#263B35") else Color.parseColor("#EAF4EE"))
            }
        }

        inputLayouts.filterNotNull().forEach { inputLayout ->
            val fieldBackground = if (isDarkTheme) Color.parseColor("#1F2937") else Color.WHITE
            val fieldText = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
            val fieldHint = if (isDarkTheme) Color.parseColor("#D1D5DB") else Color.parseColor("#58677A")
            val fieldStroke = if (isDarkTheme) Color.parseColor("#52637A") else Color.parseColor("#B8D6C4")
            inputLayout.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            inputLayout.setBoxBackgroundColor(fieldBackground)
            inputLayout.boxStrokeColor = fieldStroke
            inputLayout.boxStrokeWidth = 1.dp()
            inputLayout.boxStrokeWidthFocused = 2.dp()
            inputLayout.defaultHintTextColor = ColorStateList.valueOf(fieldHint)
            inputLayout.editText?.apply {
                setTextColor(fieldText)
                setHintTextColor(fieldHint)
            }
        }
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
