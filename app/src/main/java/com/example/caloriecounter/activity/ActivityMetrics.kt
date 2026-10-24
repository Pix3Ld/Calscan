package com.example.caloriecounter.activity

import kotlin.math.roundToInt

/**
 * Wspólne reguły odrzucania błędnych odcinków GPS i szacowania kalorii aktywności.
 */
object ActivityMetrics {
    // Odrzuca skoki lokalizacji, które oznaczałyby nierealną prędkość pieszego lub biegacza.
    private const val MAX_COUNTED_SPEED_METERS_PER_SECOND = 8.0

    // Nie dolicza odcinków, których pozycja GPS jest zbyt mało precyzyjna.
    private const val MAX_GPS_ACCURACY_METERS = 35f

    /**
     * Sprawdza, czy odcinek GPS można doliczyć do trasy na podstawie kroków, dokładności i prędkości.
     */
    fun shouldCountSegment(
        distanceMeters: Float,
        durationSeconds: Float,
        accuracyMeters: Float,
        newSteps: Int
    ): Boolean =
        newSteps > 0 &&
            distanceMeters > 0f &&
            durationSeconds > 0f &&
            accuracyMeters in 0f..MAX_GPS_ACCURACY_METERS &&
            distanceMeters / durationSeconds <= MAX_COUNTED_SPEED_METERS_PER_SECOND

    /**
     * Szacuje kalorie z wartości MET dla marszu/biegu, masy ciała i czasu aktywności.
     */
    fun estimateCalories(activityType: String, weightKg: Float, durationSeconds: Long): Int {
        val met = when (activityType) {
            ActivityTrackingService.TYPE_RUN -> 8.3
            else -> 3.5
        }
        return (met * weightKg * durationSeconds.coerceAtLeast(0) / 3600.0).roundToInt()
    }
}
