package com.example.caloriecounter.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testy reguł, które decydują o doliczaniu GPS i szacowaniu kalorii. */
class ActivityMetricsTest {
    // Odcinki bez kroków, z niedokładnym GPS lub nierealną prędkością są pomijane.
    @Test
    fun countsOnlyAccurateSegmentsWithStepsAndWalkingSpeed() {
        assertTrue(ActivityMetrics.shouldCountSegment(4f, 2f, 10f, 3))
        assertFalse(ActivityMetrics.shouldCountSegment(4f, 2f, 10f, 0))
        assertFalse(ActivityMetrics.shouldCountSegment(40f, 2f, 10f, 2))
        assertFalse(ActivityMetrics.shouldCountSegment(4f, 2f, 36f, 2))
    }

    // Wartości MET dla marszu i biegu są przeliczane na kalorie dla godziny aktywności.
    @Test
    fun estimatesCaloriesFromActivityWeightAndDuration() {
        assertEquals(245, ActivityMetrics.estimateCalories(ActivityTrackingService.TYPE_WALK, 70f, 3_600))
        assertEquals(581, ActivityMetrics.estimateCalories(ActivityTrackingService.TYPE_RUN, 70f, 3_600))
    }
}
