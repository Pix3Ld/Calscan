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
        val positiveColor = if (isDarkTheme) Color.parseColor("#6EE7B7") else Color.parseColor("#2E7D32")
        val buttonBackground = if (isDarkTheme) Color.parseColor("#2F3B52") else Color.parseColor("#E5E7EB")
        val buttonIconColor = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#111827")
        val chartCardColor = if (isDarkTheme) Color.parseColor("#243044") else Color.WHITE
        val navBg = if (isDarkTheme) Color.parseColor("#1B2738") else Color.parseColor("#E8EEF5")
        val navItemColor = if (isDarkTheme) Color.parseColor("#E5E7EB") else Color.parseColor("#2E4057")

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
                cornerRadius = 18f
                setColor(buttonBackground)
                setStroke(1, if (isDarkTheme) Color.parseColor("#3A4657") else Color.parseColor("#D1D5DB"))
            }
            elevation = 2f
            setColorFilter(buttonIconColor)
            setImageResource(if (isDarkTheme) R.drawable.ic_moon_24 else R.drawable.ic_sun_24)
        }

        listOf(R.id.todayCard, R.id.remainingCard, R.id.yesterdayCard, R.id.weekCard, R.id.chartCard)
            .forEach { id -> activity.findViewById<View>(id).setBackgroundColor(cardColor) }
        activity.findViewById<View>(R.id.chartCard).setBackgroundColor(chartCardColor)
        activity.findViewById<BottomNavigationView>(R.id.bottomNavigation).apply {
            setBackgroundColor(navBg)
            itemIconTintList = ColorStateList.valueOf(navItemColor)
            itemTextColor = ColorStateList.valueOf(navItemColor)
        }

        activity.findViewById<TextView>(R.id.todayValue).setTextColor(primaryTextColor)
        activity.findViewById<TextView>(R.id.yesterdayValue).setTextColor(primaryTextColor)
        activity.findViewById<TextView>(R.id.weekValue).setTextColor(primaryTextColor)
        activity.findViewById<TextView>(R.id.remainingValue).setTextColor(positiveColor)

        listOf(R.id.labelToday, R.id.labelRemaining, R.id.labelYesterday, R.id.labelWeek)
            .forEach { id -> activity.findViewById<TextView>(id).setTextColor(secondaryTextColor) }

        val macroTextColor = if (isDarkTheme) Color.parseColor("#E5E7EB") else Color.parseColor("#1F2937")
        listOf(R.id.proteinLabel, R.id.carbsLabel, R.id.fatLabel)
            .forEach { id -> activity.findViewById<TextView>(id).setTextColor(macroTextColor) }
        activity.findViewById<View>(R.id.proteinDot).setBackgroundColor(Color.parseColor("#E53935"))
        activity.findViewById<View>(R.id.carbsDot).setBackgroundColor(Color.parseColor("#39A0ED"))
        activity.findViewById<View>(R.id.fatDot).setBackgroundColor(Color.parseColor("#FF9800"))

        activity.findViewById<TextView>(R.id.scanHint).setTextColor(
            if (isDarkTheme) Color.parseColor("#B5E0FF") else Color.parseColor("#1C5DD9")
        )
        activity.findViewById<View>(R.id.scanHint).setBackgroundColor(
            if (isDarkTheme) Color.parseColor("#1B3A5B") else Color.parseColor("#E6F0FF")
        )

        reminderHomeNotice?.apply {
            setTextColor(if (isDarkTheme) Color.parseColor("#B5E0FF") else Color.parseColor("#1C5DD9"))
            background = GradientDrawable().apply {
                cornerRadius = 8.dp().toFloat()
                setColor(if (isDarkTheme) Color.parseColor("#1B3A5B") else Color.parseColor("#E6F0FF"))
            }
        }

        inputLayouts.filterNotNull().forEach { inputLayout ->
            val fieldBackground = if (isDarkTheme) Color.parseColor("#1F2937") else Color.WHITE
            val fieldText = if (isDarkTheme) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430")
            val fieldHint = if (isDarkTheme) Color.parseColor("#D1D5DB") else Color.parseColor("#58677A")
            val fieldStroke = if (isDarkTheme) Color.parseColor("#8AB89D") else Color.parseColor("#1E654C")
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
