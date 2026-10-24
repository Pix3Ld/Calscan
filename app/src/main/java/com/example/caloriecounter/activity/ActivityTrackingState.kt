package com.example.caloriecounter.activity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Punkt trasy GPS zapisany jako szerokość i długość geograficzna. */
data class TrackCoordinate(val latitude: Double, val longitude: Double)

/** Niezmienna migawka danych sesji przekazywana między usługą i interfejsem. */
data class ActivitySnapshot(
    val isTracking: Boolean = false,
    val activityType: String = ActivityTrackingService.TYPE_WALK,
    val elapsedSeconds: Long = 0,
    val distanceMeters: Double = 0.0,
    val steps: Int = 0,
    val caloriesKcal: Int = 0,
    val routeSegments: List<List<TrackCoordinate>> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Wspólny, obserwowalny stan śledzenia; zapis może aktualizować wyłącznie warstwa aktywności.
 */
object ActivityTrackingState {
    private val mutableSnapshot = MutableStateFlow(ActivitySnapshot())
    val snapshots = mutableSnapshot.asStateFlow()

    /** Publikuje kompletną migawkę odczytywaną przez ekran śledzenia. */
    internal fun publish(snapshot: ActivitySnapshot) {
        mutableSnapshot.value = snapshot
    }

    /** Zatrzymuje widoczny stan sesji i udostępnia komunikat błędu ekranowi. */
    internal fun reportError(message: String) {
        mutableSnapshot.value = mutableSnapshot.value.copy(
            isTracking = false,
            errorMessage = message
        )
    }
}
