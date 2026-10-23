package com.example.caloriecounter.settings

import android.content.Context

/**
 * Zapisuje ustawienia aplikacji: dzienny limit kalorii i wybrany motyw.
 */
class AppSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun dailyLimit(): Int =
        preferences.getInt(KEY_DAILY_LIMIT, DEFAULT_DAILY_LIMIT).coerceAtLeast(1)

    fun isDarkThemeEnabled(): Boolean = preferences.getBoolean(KEY_DARK_THEME, false)

    fun saveDailyLimit(limit: Int) {
        preferences.edit().putInt(KEY_DAILY_LIMIT, limit).apply()
    }

    fun saveDarkTheme(enabled: Boolean): Boolean =
        preferences.edit().putBoolean(KEY_DARK_THEME, enabled).commit()

    companion object {
        const val DEFAULT_DAILY_LIMIT = 2200
        private const val PREFERENCES_NAME = "calorie_counter_settings"
        private const val KEY_DAILY_LIMIT = "daily_limit"
        private const val KEY_DARK_THEME = "dark_theme"
    }
}
