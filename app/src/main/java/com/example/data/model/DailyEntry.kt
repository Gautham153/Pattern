package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyEntry(
    val date: String, // format "yyyy-MM-dd" e.g. "2025-04-26"
    val sleepHours: Double,
    val screenTimeHours: Double,
    val studyHours: Double,
    val exerciseMinutes: Double,
    val mood: Double,
    val productivity: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun getMetricValue(metricType: MetricType): Double {
        return when (metricType) {
            MetricType.SLEEP -> sleepHours
            MetricType.SCREEN_TIME -> screenTimeHours
            MetricType.STUDY -> studyHours
            MetricType.EXERCISE -> exerciseMinutes
            MetricType.MOOD -> mood
            MetricType.PRODUCTIVITY -> productivity
        }
    }

    fun formattedDateShort(): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatter = SimpleDateFormat("MMM d", Locale.US)
            val d = parser.parse(date)
            if (d != null) formatter.format(d) else date
        } catch (e: Exception) {
            date
        }
    }

    fun formattedDateFull(): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val d = parser.parse(date)
            if (d != null) formatter.format(d) else date
        } catch (e: Exception) {
            date
        }
    }

    companion object {
        fun todayDateString(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return formatter.format(Date())
        }
    }
}
