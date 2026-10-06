package com.example

import com.example.data.analytics.PatternAnalytics
import com.example.data.model.AnomalySeverity
import com.example.data.model.DailyEntry
import com.example.data.model.MetricType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    // Test 1: Normal observation (|z| < 2.0)
    @Test
    fun testCase1_normalObservation() {
        // Construct 8 baseline values with mean = 5.0 and stdDev = 1.0
        // e.g., values: 4, 4, 5, 5, 5, 5, 6, 6 -> mean = 5.0, variance = 4/7, etc.
        // Let's use clean values:
        val baseline = listOf(3.0, 4.0, 4.0, 5.0, 5.0, 6.0, 6.0, 7.0) // mean = 5.0
        val stdDev = PatternAnalytics.calculateStandardDeviation(baseline)
        // observed value = 5.5 -> z = 0.5 / stdDev < 1.0 -> NORMAL
        val result = PatternAnalytics.detectAnomaly(5.5, baseline)
        assertEquals(AnomalySeverity.NORMAL, result.severity)
        assertTrue(abs(result.zScore) < 2.0)
        assertFalse(result.isFallback)
    }

    // Test 2: z-score exactly 2.0 -> Unusual
    @Test
    fun testCase2_zScoreExactlyTwoPointZero_isUnusual() {
        val mean = 10.0
        val stdDev = 2.0
        // Baseline of 8 values centered at 10 with stdDev ~ 2.0
        // Or directly evaluate with values yielding exact mean and stdDev:
        // Consider: 8, 8, 10, 10, 10, 10, 12, 12 -> mean = 10.0
        // variance = (4+4+0+0+0+0+4+4)/7 = 16/7 -> stdDev = sqrt(16/7) = 1.51185789...
        // Let's construct baseline where we test value = mean + 2.0 * stdDev:
        val baseline = listOf(6.0, 8.0, 9.0, 10.0, 10.0, 11.0, 12.0, 14.0)
        val calcMean = PatternAnalytics.calculateMean(baseline)
        val calcStd = PatternAnalytics.calculateStandardDeviation(baseline, calcMean)

        val targetVal = calcMean + 2.0 * calcStd
        val result = PatternAnalytics.detectAnomaly(targetVal, baseline)

        assertEquals(2.0, abs(result.zScore), 0.001)
        assertEquals(AnomalySeverity.UNUSUAL, result.severity)
    }

    // Test 3: z-score >= 2.8 -> Very unusual
    @Test
    fun testCase3_zScoreGreaterThanOrEqualToTwoPointEight_isVeryUnusual() {
        val baseline = listOf(6.0, 8.0, 9.0, 10.0, 10.0, 11.0, 12.0, 14.0)
        val calcMean = PatternAnalytics.calculateMean(baseline)
        val calcStd = PatternAnalytics.calculateStandardDeviation(baseline, calcMean)

        val targetVal = calcMean + 2.85 * calcStd
        val result = PatternAnalytics.detectAnomaly(targetVal, baseline)

        assertTrue(abs(result.zScore) >= 2.8)
        assertEquals(AnomalySeverity.VERY_UNUSUAL, result.severity)
    }

    // Test 4: Current observation is excluded from its own baseline
    @Test
    fun testCase4_currentObservationExcludedFromItsOwnBaseline() {
        // Construct 8 entries: Days 1–7 are normal (Screen Time = 4.0h), Day 8 has 16.0h.
        val entries = mutableListOf<DailyEntry>()
        for (i in 1..7) {
            entries.add(
                DailyEntry(
                    date = "2025-04-0$i",
                    sleepHours = 7.0,
                    screenTimeHours = 4.0 + (if (i % 2 == 0) 0.2 else -0.2), // tight normal range
                    studyHours = 3.0,
                    exerciseMinutes = 30.0,
                    mood = 7.0,
                    productivity = 7.0
                )
            )
        }
        val day8 = DailyEntry(
            date = "2025-04-08",
            sleepHours = 7.0,
            screenTimeHours = 14.0, // High outlier
            studyHours = 3.0,
            exerciseMinutes = 30.0,
            mood = 7.0,
            productivity = 7.0
        )
        entries.add(day8)

        // If Day 8 were included in its own baseline, Day 8's mean would be pulled up to ~5.25h
        // and its standard deviation inflated to ~3.5h.
        // But with exclusion:
        val anomaliesDay8 = PatternAnalytics.detectAnomaliesForEntry(day8, entries)
        val screenAnomaly = anomaliesDay8.find { it.metric == MetricType.SCREEN_TIME }

        assertTrue(screenAnomaly != null)
        // Baseline mean should be ~4.0h, NOT inflated by the 14.0h value!
        assertEquals(4.0, screenAnomaly!!.baselineMean, 0.1)
        assertTrue(screenAnomaly.zScore > 5.0)
        assertEquals(AnomalySeverity.VERY_UNUSUAL, screenAnomaly.severity)
    }

    // Test 5: Zero standard deviation (identical historical values)
    @Test
    fun testCase5_zeroStandardDeviation() {
        val baseline = listOf(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0)

        // Same value -> Normal
        val normalResult = PatternAnalytics.detectAnomaly(5.0, baseline)
        assertEquals(AnomalySeverity.NORMAL, normalResult.severity)
        assertEquals(0.0, normalResult.zScore, 0.001)
        assertEquals(0.0, normalResult.percentageDifference, 0.001)

        // Value deviates by 40% (e.g. 7.0) -> Unusual via fallback
        val unusualResult = PatternAnalytics.detectAnomaly(7.0, baseline)
        assertEquals(AnomalySeverity.UNUSUAL, unusualResult.severity)
        assertEquals(40.0, unusualResult.percentageDifference, 0.001)
        assertFalse(unusualResult.percentageDifference.isNaN())
        assertFalse(unusualResult.percentageDifference.isInfinite())

        // Value deviates by 100% (e.g. 10.0) -> Very unusual via fallback
        val veryUnusualResult = PatternAnalytics.detectAnomaly(10.0, baseline)
        assertEquals(AnomalySeverity.VERY_UNUSUAL, veryUnusualResult.severity)
        assertEquals(100.0, veryUnusualResult.percentageDifference, 0.001)
    }

    // Test 6: Fewer than 7 historical observations (Fallback logic)
    @Test
    fun testCase6_fewerThanSevenHistoricalObservations() {
        val smallBaseline = listOf(5.0, 5.0, 5.0, 5.0) // 4 observations (< 7)

        val resultNormal = PatternAnalytics.detectAnomaly(5.5, smallBaseline) // 10% diff
        assertEquals(AnomalySeverity.NORMAL, resultNormal.severity)
        assertTrue(resultNormal.isFallback)
        assertEquals(0.0, resultNormal.zScore, 0.001)

        val resultUnusual = PatternAnalytics.detectAnomaly(7.0, smallBaseline) // 40% diff (>= 35%)
        assertEquals(AnomalySeverity.UNUSUAL, resultUnusual.severity)
        assertTrue(resultUnusual.isFallback)

        val resultVeryUnusual = PatternAnalytics.detectAnomaly(9.0, smallBaseline) // 80% diff (>= 65%)
        assertEquals(AnomalySeverity.VERY_UNUSUAL, resultVeryUnusual.severity)
        assertTrue(resultVeryUnusual.isFallback)
    }

    // Test 7: Correlation with zero variance
    @Test
    fun testCase7_correlationWithZeroVariance() {
        val constantX = listOf(5.0, 5.0, 5.0, 5.0, 5.0, 5.0)
        val varyingY = listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        val r = PatternAnalytics.calculatePearsonCorrelation(constantX, varyingY)
        assertEquals(0.0, r, 0.001)
        assertFalse(r.isNaN())
        assertFalse(r.isInfinite())
    }

    // Test 8: Pattern Score remains strictly between 0 and 100
    @Test
    fun testCase8_patternScoreRemainsBetweenZeroAndOneHundred() {
        // Case 8a: Empty entries
        assertEquals(100, PatternAnalytics.calculatePatternScore(emptyList(), emptyList()))

        // Case 8b: Perfectly normal entries
        val normalEntries = (1..10).map { i ->
            DailyEntry(
                date = "2025-04-%02d".format(i),
                sleepHours = 7.0,
                screenTimeHours = 4.5,
                studyHours = 3.0,
                exerciseMinutes = 30.0,
                mood = 7.0,
                productivity = 7.0
            )
        }
        val normalScore = PatternAnalytics.calculatePatternScore(normalEntries.take(7), normalEntries)
        assertEquals(100, normalScore)

        // Case 8c: Severely anomalous entries
        val extremeEntries = (1..10).map { i ->
            DailyEntry(
                date = "2025-04-%02d".format(i),
                sleepHours = if (i == 10) 0.0 else 8.0,
                screenTimeHours = if (i == 10) 16.0 else 4.0,
                studyHours = if (i == 10) 0.0 else 6.0,
                exerciseMinutes = if (i == 10) 240.0 else 30.0,
                mood = if (i == 10) 1.0 else 9.0,
                productivity = if (i == 10) 1.0 else 9.0
            )
        }
        // Evaluate the extreme day (Day 10) where all 6 metrics are outliers
        val scoreForExtremeDay = PatternAnalytics.calculatePatternScore(listOf(extremeEntries.last()), extremeEntries)
        assertEquals(0, scoreForExtremeDay)

        // Evaluate all 10 days containing the extreme day
        val scoreOverall = PatternAnalytics.calculatePatternScore(extremeEntries, extremeEntries)
        assertTrue(scoreOverall in 0..100)
        assertTrue(scoreOverall < 100)
    }
}
