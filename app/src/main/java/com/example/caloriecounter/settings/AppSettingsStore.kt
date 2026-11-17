package com.example.caloriecounter.settings

import android.content.Context

data class CalorieProfile(
    val weightKg: Float,
    val heightCm: Int,
    val ageYears: Int,
    val sex: String,
    val activityLevel: Int
)

/**
 * Zapisuje ustawienia aplikacji: dzienny limit, profil, tryb liczenia kalorii i motyw.
 */
class AppSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun dailyLimit(): Int =
        preferences.getInt(KEY_DAILY_LIMIT, DEFAULT_DAILY_LIMIT).coerceAtLeast(1)

    fun isDarkThemeEnabled(): Boolean = preferences.getBoolean(KEY_DARK_THEME, false)

    fun calorieProfile(): CalorieProfile = CalorieProfile(
        weightKg = preferences.getFloat(KEY_WEIGHT_KG, DEFAULT_WEIGHT_KG),
        heightCm = preferences.getInt(KEY_HEIGHT_CM, 0),
        ageYears = preferences.getInt(KEY_AGE_YEARS, 0),
        sex = preferences.getString(KEY_SEX, "").orEmpty(),
        activityLevel = preferences.getInt(KEY_ACTIVITY_LEVEL, DEFAULT_ACTIVITY_LEVEL)
            .coerceIn(0, ACTIVITY_LEVEL_MULTIPLIERS.lastIndex)
    )

    fun isSmartCalorieCalculationEnabled(): Boolean =
        preferences.getBoolean(KEY_SMART_CALORIES, false)

    fun saveDailyLimit(limit: Int) {
        preferences.edit().putInt(KEY_DAILY_LIMIT, limit).apply()
    }

    fun saveCalorieProfile(profile: CalorieProfile) {
        preferences.edit()
            .putFloat(KEY_WEIGHT_KG, profile.weightKg)
            .putInt(KEY_HEIGHT_CM, profile.heightCm)
            .putInt(KEY_AGE_YEARS, profile.ageYears)
            .putString(KEY_SEX, profile.sex)
            .putInt(KEY_ACTIVITY_LEVEL, profile.activityLevel)
            .apply()
    }

    fun saveSmartCalorieCalculationEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_SMART_CALORIES, enabled).apply()
    }

    fun saveDarkTheme(enabled: Boolean): Boolean =
        preferences.edit().putBoolean(KEY_DARK_THEME, enabled).commit()

    companion object {
        const val DEFAULT_DAILY_LIMIT = 2200
        const val DEFAULT_WEIGHT_KG = 70f
        const val DEFAULT_ACTIVITY_LEVEL = 2
        val ACTIVITY_LEVEL_MULTIPLIERS = listOf(1.2, 1.375, 1.55, 1.725, 1.9)
        private const val PREFERENCES_NAME = "calorie_counter_settings"
        private const val KEY_DAILY_LIMIT = "daily_limit"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_WEIGHT_KG = "weight_kg"
        private const val KEY_HEIGHT_CM = "height_cm"
        private const val KEY_AGE_YEARS = "age_years"
        private const val KEY_SEX = "sex"
        private const val KEY_ACTIVITY_LEVEL = "activity_level"
        private const val KEY_SMART_CALORIES = "smart_calories"
    }
}
