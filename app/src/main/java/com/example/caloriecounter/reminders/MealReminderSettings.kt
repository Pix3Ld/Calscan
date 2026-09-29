package com.example.caloriecounter.reminders

import android.content.Context

 data class MealReminderSettings(
    val enabled: Boolean = false,
    val mealCount: Int = ReminderTimeCalculator.DEFAULT_MEALS,
    val mealTimesMinutes: List<Int> = ReminderTimeCalculator.defaultTimes(mealCount),
    val hasConfiguredMeals: Boolean = false
)

class MealReminderSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun load(): MealReminderSettings {
        val mealCount = preferences.getInt(KEY_MEAL_COUNT, ReminderTimeCalculator.DEFAULT_MEALS)
            .coerceIn(ReminderTimeCalculator.MIN_MEALS, ReminderTimeCalculator.MAX_MEALS)
        val savedTimes = preferences.getString(KEY_MEAL_TIMES, null)
            ?.split(',')
            ?.mapNotNull(String::toIntOrNull)
            ?.takeIf { times ->
                times.size == mealCount && times.all { it in 0 until MINUTES_PER_DAY }
            }
            ?: ReminderTimeCalculator.defaultTimes(mealCount)

        return MealReminderSettings(
            enabled = preferences.getBoolean(KEY_ENABLED, false),
            mealCount = mealCount,
            mealTimesMinutes = savedTimes,
            hasConfiguredMeals = preferences.getBoolean(KEY_HAS_CONFIGURED_MEALS, false)
        )
    }

    fun save(settings: MealReminderSettings) {
        require(settings.mealCount in ReminderTimeCalculator.MIN_MEALS..ReminderTimeCalculator.MAX_MEALS)
        require(settings.mealTimesMinutes.size == settings.mealCount)
        require(settings.mealTimesMinutes.all { it in 0 until MINUTES_PER_DAY })

        preferences.edit()
            .putBoolean(KEY_ENABLED, settings.enabled)
            .putInt(KEY_MEAL_COUNT, settings.mealCount)
            .putString(KEY_MEAL_TIMES, settings.mealTimesMinutes.joinToString(","))
            .putBoolean(KEY_HAS_CONFIGURED_MEALS, settings.hasConfiguredMeals)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "meal_reminders"
        const val KEY_ENABLED = "enabled"
        const val KEY_MEAL_COUNT = "meal_count"
        const val KEY_MEAL_TIMES = "meal_times_minutes"
        const val KEY_HAS_CONFIGURED_MEALS = "has_configured_meals"
        const val MINUTES_PER_DAY = 24 * 60
    }
}
