package com.example.caloriecounter.settings

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.example.caloriecounter.reminders.MealReminderSettingsStore

/**
 * Tworzy stronę ustawień oraz karty otwierające szczegółowe konfiguracje.
 */
class SettingsPageRenderer(
    private val activity: AppCompatActivity,
    private val reminderSettingsStore: MealReminderSettingsStore,
    private val isDarkTheme: () -> Boolean,
    private val pageText: (String, Float, Boolean) -> TextView
) {
    // Buduje karty ustawień i zwraca tekst limitu, aby aktywność mogła go odświeżać po zapisie.
    fun render(
        container: LinearLayout,
        dailyLimit: Int,
        onRemindersClick: () -> Unit,
        onDailyLimitClick: () -> Unit
    ): TextView {
        container.addView(pageText(activity.getString(R.string.settings_page_intro), 16f, true))
        val settings = reminderSettingsStore.load()
        val reminderSummary = if (settings.enabled) {
            activity.getString(R.string.settings_reminders_summary_on, settings.mealCount)
        } else {
            activity.getString(R.string.settings_reminders_summary_off)
        }
        container.addView(settingsOptionRow(
            activity.getString(R.string.settings_reminders_title),
            reminderSummary,
            onClick = onRemindersClick
        ))

        val limitSummary = pageText(
            activity.getString(R.string.settings_daily_limit_summary, dailyLimit),
            14f,
            false
        )
        container.addView(settingsOptionRow(
            activity.getString(R.string.settings_daily_limit),
            activity.getString(R.string.settings_daily_limit_summary, dailyLimit),
            limitSummary,
            onClick = onDailyLimitClick
        ))
        return limitSummary
    }

    private fun settingsOptionRow(
        title: String,
        summary: String,
        summaryView: TextView = pageText(summary, 14f, false),
        onClick: () -> Unit
    ): View = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(16.dp(), 14.dp(), 12.dp(), 14.dp())
        background = GradientDrawable().apply {
            cornerRadius = 16.dp().toFloat()
            setColor(if (isDarkTheme()) Color.parseColor("#1F2937") else Color.WHITE)
            setStroke(1.dp(), if (isDarkTheme()) Color.parseColor("#344238") else Color.parseColor("#D8E8DF"))
        }.let { card ->
            RippleDrawable(
                ColorStateList.valueOf(
                    if (isDarkTheme()) Color.parseColor("#344238") else Color.parseColor("#E6F3ED")
                ),
                card,
                null
            )
        }
        elevation = 2.dp().toFloat()
        isClickable = true
        isFocusable = true
        contentDescription = "$title. $summary"
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 12.dp(), 0, 0) }

        val textContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        textContainer.addView(pageText(title, 16f, true))
        summaryView.text = summary
        summaryView.setTextColor(
            if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586")
        )
        summaryView.setPadding(0, 4.dp(), 0, 0)
        textContainer.addView(summaryView)
        addView(textContainer)
        addView(TextView(activity).apply {
            text = "›"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586"))
        }, LinearLayout.LayoutParams(32.dp(), ViewGroup.LayoutParams.MATCH_PARENT))
        setOnClickListener { onClick() }
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
