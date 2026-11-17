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
        val remaining = dailyLimit - todayConsumed
        activity.findViewById<TextView>(R.id.remainingValue).apply {
            text = activity.getString(R.string.remaining_value_template, remaining)
            setTextColor(
                when {
                    remaining < 0 && isDarkTheme() -> Color.parseColor("#FDA4AF")
                    remaining < 0 -> Color.parseColor("#B42318")
                    isDarkTheme() -> Color.parseColor("#6EE7B7")
                    else -> Color.parseColor("#1E654C")
                }
            )
        }
        activity.findViewById<TextView>(R.id.proteinLabel).text =
            activity.getString(R.string.macro_protein_template, todayProtein)
        activity.findViewById<TextView>(R.id.carbsLabel).text =
            activity.getString(R.string.macro_carbs_template, todayCarbs)
        activity.findViewById<TextView>(R.id.fatLabel).text =
            activity.getString(R.string.macro_fat_template, todayFat)
    }

    // Rysuje prosty wykres dziennego spożycia kalorii z podziałem na makroskładniki.
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

        val proteinColor = if (isDarkTheme()) Color.parseColor("#6EE7B7") else Color.parseColor("#1E654C")
        val carbsColor = if (isDarkTheme()) Color.parseColor("#F87171") else Color.parseColor("#DC2626")
        val fatColor = if (isDarkTheme()) Color.parseColor("#FBBF24") else Color.parseColor("#D97706")
        val emptyColor = if (isDarkTheme()) Color.parseColor("#374151") else Color.parseColor("#E5E7EB")

        dayData.forEach { (dayLabel, value, macros) ->
            val column = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(6, 0, 6, 0)
                }
            }

            val proteinGrams = macros.first
            val carbsGrams = macros.second
            val fatGrams = macros.third
            val totalMacroGrams = proteinGrams + carbsGrams + fatGrams

            val barHeight = if (value > 0) {
                ((value.toFloat() / maxCalories.toFloat()) * 92f).toInt().coerceIn(8, 92)
            } else {
                3
            }

            val barStack = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    barHeight.dp()
                )
            }

            if (value > 0 && totalMacroGrams > 0f) {
                val fatHeight = ((fatGrams / totalMacroGrams) * barHeight).toInt().coerceAtLeast(2)
                val carbsHeight = ((carbsGrams / totalMacroGrams) * barHeight).toInt().coerceAtLeast(2)
                val proteinHeight = ((proteinGrams / totalMacroGrams) * barHeight).toInt().coerceAtLeast(2)

                val fatBar = View(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fatHeight.dp())
                    background = ColorDrawable(fatColor)
                }
                val carbsBar = View(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, carbsHeight.dp())
                    background = ColorDrawable(carbsColor)
                }
                val proteinBar = View(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, proteinHeight.dp())
                    background = ColorDrawable(proteinColor)
                }

                barStack.addView(fatBar)
                barStack.addView(carbsBar)
                barStack.addView(proteinBar)
            } else {
                val singleBar = View(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        barHeight.dp()
                    )
                    background = ColorDrawable(if (value > 0) proteinColor else emptyColor)
                }
                barStack.addView(singleBar)
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
