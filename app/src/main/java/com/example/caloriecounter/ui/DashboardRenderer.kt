package com.example.caloriecounter.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriecounter.R
import com.example.caloriecounter.data.MealSummaryEntity
import java.util.Calendar

/**
 * Aktualizuje wartości podsumowania i buduje wykres tygodniowy na ekranie głównym.
 */
class DashboardRenderer(
    private val activity: AppCompatActivity,
    private val isDarkTheme: () -> Boolean
) {
    // Przelicza kalorie i makroskładniki dla dzisiaj, wczoraj oraz bieżącego tygodnia.
    fun updateDashboard(summaries: List<MealSummaryEntity>, dailyLimit: Int) {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = todayCal.timeInMillis
        val todayEnd = todayStart + 24 * 3600 * 1000L - 1L
        val yesterdayStart = todayStart - 24 * 3600 * 1000L
        val yesterdayEnd = todayStart - 1L

        val todaySummaries = summaries.filter { it.createdAt in todayStart..todayEnd }
        val todayConsumed = todaySummaries.sumOf { it.caloriesKcal }
        val todayProtein = Math.round(todaySummaries.sumOf { it.proteinGrams.toDouble() }).toInt()
        val todayCarbs = Math.round(todaySummaries.sumOf { it.carbohydratesGrams.toDouble() }).toInt()
        val todayFat = Math.round(todaySummaries.sumOf { it.fatGrams.toDouble() }).toInt()
        val yesterdayConsumed = summaries
            .filter { it.createdAt in yesterdayStart..yesterdayEnd }
            .sumOf { it.caloriesKcal }
        val weekRanges = getWeekDayRanges()
        val weekTotal = summaries
            .filter { summary -> weekRanges.any { summary.createdAt in it.second } }
            .sumOf { it.caloriesKcal }

        activity.findViewById<TextView>(R.id.todayValue).text =
            activity.getString(R.string.today_value_template, todayConsumed)
        activity.findViewById<TextView>(R.id.yesterdayValue).text =
            activity.getString(R.string.yesterday_value_template, yesterdayConsumed)
        activity.findViewById<TextView>(R.id.weekValue).text =
            activity.getString(R.string.week_value_template, weekTotal)
        activity.findViewById<TextView>(R.id.remainingValue).text =
            activity.getString(R.string.remaining_value_template, dailyLimit - todayConsumed)
        activity.findViewById<TextView>(R.id.proteinLabel).text =
            activity.getString(R.string.macro_protein_template, todayProtein)
        activity.findViewById<TextView>(R.id.carbsLabel).text =
            activity.getString(R.string.macro_carbs_template, todayCarbs)
        activity.findViewById<TextView>(R.id.fatLabel).text =
            activity.getString(R.string.macro_fat_template, todayFat)
    }

    // Rysuje słupki kalorii z podziałem na udział białka, węglowodanów i tłuszczów.
    fun buildChart(summaries: List<MealSummaryEntity>) {
        val dayData = getWeekDayRanges().map { (dayName, range) ->
            val daySummaries = summaries.filter { it.createdAt in range }
            val calories = daySummaries.sumOf { it.caloriesKcal }
            val protein = daySummaries.sumOf { it.proteinGrams.toDouble() }.toFloat()
            val carbs = daySummaries.sumOf { it.carbohydratesGrams.toDouble() }.toFloat()
            val fat = daySummaries.sumOf { it.fatGrams.toDouble() }.toFloat()
            Triple(dayName, calories, Triple(protein, carbs, fat))
        }

        val maxCalories = dayData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
        val chartContainer = activity.findViewById<LinearLayout>(R.id.chartContainer)
        chartContainer.removeAllViews()

        dayData.forEach { (dayLabel, value, macros) ->
            val column = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(6, 0, 6, 0)
                }
            }

            val proteinValue = macros.first
            val carbsValue = macros.second
            val fatValue = macros.third
            val totalMacro = proteinValue + carbsValue + fatValue
            val barHeight = if (value > 0) {
                ((value.toFloat() / maxCalories.toFloat()) * 140f).toInt().coerceIn(12, 140)
            } else {
                4
            }
            val barStack = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    barHeight.dp()
                )
            }

            if (value > 0 && totalMacro > 0f) {
                val proteinHeight = ((proteinValue / totalMacro) * barHeight).toInt().coerceAtLeast(2)
                val carbsHeight = ((carbsValue / totalMacro) * barHeight).toInt().coerceAtLeast(2)
                val fatHeight = ((fatValue / totalMacro) * barHeight).toInt().coerceAtLeast(2)
                barStack.addView(macroBar("#FF9800", fatHeight))
                barStack.addView(macroBar("#39A0ED", carbsHeight))
                barStack.addView(macroBar("#E53935", proteinHeight))
            } else {
                val emptyColor = if (value > 0) {
                    Color.parseColor("#39A0ED")
                } else if (isDarkTheme()) {
                    Color.parseColor("#374151")
                } else {
                    Color.parseColor("#E5E7EB")
                }
                barStack.addView(View(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        barHeight.dp()
                    )
                    background = ColorDrawable(emptyColor)
                })
            }

            column.addView(TextView(activity).apply {
                text = value.toString()
                gravity = Gravity.CENTER
                textSize = 11f
                setTextColor(if (isDarkTheme()) Color.parseColor("#E5E7EB") else Color.parseColor("#1F2937"))
            })
            column.addView(barStack)
            column.addView(TextView(activity).apply {
                text = dayLabel
                gravity = Gravity.CENTER
                setTextAppearance(android.R.style.TextAppearance_Material_Caption)
                setTextColor(if (isDarkTheme()) Color.parseColor("#D1D5DB") else Color.parseColor("#697586"))
            })
            chartContainer.addView(column)
        }
    }

    private fun macroBar(color: String, height: Int): View = View(activity).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height.dp()
        )
        background = ColorDrawable(Color.parseColor(color))
    }

    private fun getWeekDayRanges(): List<Pair<String, LongRange>> {
        val dayNames = listOf("P", "W", "Ś", "C", "P", "S", "N")
        val calendar = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY }
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return (0 until 7).map { index ->
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val end = calendar.timeInMillis - 1
            dayNames[index] to (start..end)
        }
    }

    private fun Int.dp(): Int = (this * activity.resources.displayMetrics.density).toInt()
}
