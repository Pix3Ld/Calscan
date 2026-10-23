package com.example.caloriecounter.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * Tworzy wspólne elementy tekstowe i przyciski Material używane na dynamicznych ekranach.
 */
class ActivityViewFactory(
    private val activity: AppCompatActivity,
    private val isDarkTheme: () -> Boolean
) {
    fun pageText(text: String, size: Float, bold: Boolean): TextView =
        TextView(activity).apply {
            this.text = text
            textSize = size
            setTextColor(if (isDarkTheme()) Color.parseColor("#F3F4F6") else Color.parseColor("#1C2430"))
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    fun styledButton(text: String, primary: Boolean): MaterialButton {
        val brandColor = Color.parseColor("#1E654C")
        val textColor = when {
            primary -> Color.WHITE
            isDarkTheme() -> Color.parseColor("#D7F3E3")
            else -> brandColor
        }
        val backgroundColor = when {
            primary -> brandColor
            isDarkTheme() -> Color.parseColor("#263B35")
            else -> Color.parseColor("#EAF4EE")
        }

        return MaterialButton(activity).apply {
            this.text = text
            isAllCaps = false
            textSize = 15f
            cornerRadius = 16.dp()
            insetTop = 0
            insetBottom = 0
            setPadding(20.dp(), 10.dp(), 20.dp(), 10.dp())
            backgroundTintList = ColorStateList.valueOf(backgroundColor)
            setTextColor(textColor)
            strokeWidth = if (primary) 0 else 1.dp()
            strokeColor = ColorStateList.valueOf(
                if (isDarkTheme()) Color.parseColor("#527565") else Color.parseColor("#B8D6C4")
            )
            rippleColor = ColorStateList.valueOf(
                if (primary) Color.parseColor("#3C8067") else Color.parseColor("#C9E2D2")
            )
            elevation = 2.dp().toFloat()
        }
    }

    fun dpValue(value: Int): Int = (value * activity.resources.displayMetrics.density).toInt()

    private fun Int.dp(): Int = dpValue(this)
}
