package com.example.caloriecounter.settings

import kotlin.math.roundToInt

/** Szacuje dzienne zapotrzebowanie na podstawie wzoru Mifflina–St Jeora. */
object MaintenanceCalorieCalculator {
    fun calculate(profile: CalorieProfile): Int {
        require(profile.weightKg in 20f..300f)
        require(profile.heightCm in 100..250)
        require(profile.ageYears in 13..120)
        require(profile.sex == SEX_MALE || profile.sex == SEX_FEMALE)
        require(profile.activityLevel in AppSettingsStore.ACTIVITY_LEVEL_MULTIPLIERS.indices)

        val sexAdjustment = if (profile.sex == SEX_MALE) 5 else -161
        val restingCalories = 10 * profile.weightKg +
            6.25 * profile.heightCm -
            5 * profile.ageYears +
            sexAdjustment
        return (restingCalories * AppSettingsStore.ACTIVITY_LEVEL_MULTIPLIERS[profile.activityLevel])
            .roundToInt()
    }

    const val SEX_MALE = "male"
    const val SEX_FEMALE = "female"
}
