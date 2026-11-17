package com.example.caloriecounter.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class MaintenanceCalorieCalculatorTest {
    @Test
    fun calculatesMaintenanceCaloriesForBiologicalSex() {
        val maleProfile = CalorieProfile(
            weightKg = 70f,
            heightCm = 170,
            ageYears = 30,
            sex = MaintenanceCalorieCalculator.SEX_MALE,
            activityLevel = 2
        )
        val femaleProfile = maleProfile.copy(sex = MaintenanceCalorieCalculator.SEX_FEMALE)

        assertEquals(2507, MaintenanceCalorieCalculator.calculate(maleProfile))
        assertEquals(2250, MaintenanceCalorieCalculator.calculate(femaleProfile))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncompleteProfile() {
        MaintenanceCalorieCalculator.calculate(
            CalorieProfile(
                weightKg = 70f,
                heightCm = 0,
                ageYears = 0,
                sex = "",
                activityLevel = AppSettingsStore.DEFAULT_ACTIVITY_LEVEL
            )
        )
    }
}
