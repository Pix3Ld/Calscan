package com.example.caloriecounter.reminders

import java.util.Calendar
import java.util.TimeZone

object ReminderTimeCalculator {
    const val MIN_MEALS = 2
    const val MAX_MEALS = 5
    const val DEFAULT_MEALS = 3

    private const val START_MINUTE = 8 * 60
    private const val END_MINUTE = 20 * 60
    private const val MINUTES_PER_DAY = 24 * 60

    fun defaultTimes(mealCount: Int): List<Int> {
        require(mealCount in MIN_MEALS..MAX_MEALS)
        val span = END_MINUTE - START_MINUTE
        return (0 until mealCount).map { index ->
            START_MINUTE + span * index / (mealCount - 1)
        }
    }

    fun nextOccurrenceMillis(
        timeOfDayMinutes: Int,
        afterMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        require(timeOfDayMinutes in 0 until MINUTES_PER_DAY)

        val next = Calendar.getInstance(timeZone).apply {
            timeInMillis = afterMillis
            set(Calendar.HOUR_OF_DAY, timeOfDayMinutes / 60)
            set(Calendar.MINUTE, timeOfDayMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= afterMillis) add(Calendar.DAY_OF_MONTH, 1)
        }
        return next.timeInMillis
    }
}