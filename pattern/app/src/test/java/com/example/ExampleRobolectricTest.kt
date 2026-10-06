package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.analytics.PatternAnalytics
import com.example.data.model.AnomalySeverity
import com.example.data.model.DailyEntry
import com.example.data.model.MetricType
import com.example.data.sample.SampleDataGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Pattern", appName)
    }

    @Test
    fun `verify sample data baseline and statistics calculations`() {
        val sampleDays = SampleDataGenerator.generateSampleDays()
        assertEquals(28, sampleDays.size)

        val statsMap = PatternAnalytics.calculateAllStats(sampleDays)
        val sleepStats = statsMap[MetricType.SLEEP]
        assertTrue(sleepStats != null)
        assertTrue(sleepStats!!.mean in 6.0..8.5)
        assertTrue(sleepStats.stdDev > 0.0)

        val anomalies = PatternAnalytics.getAllAnomalies(sampleDays)
        assertTrue(anomalies.isNotEmpty())

        val patternScore = PatternAnalytics.calculatePatternScore(sampleDays.take(7), sampleDays)
        assertTrue(patternScore in 50..100)

        val correlations = PatternAnalytics.generateCorrelations(sampleDays)
        assertTrue(correlations.isNotEmpty())
    }
}
