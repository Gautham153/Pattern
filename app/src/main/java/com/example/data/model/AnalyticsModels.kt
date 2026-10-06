package com.example.data.model

enum class AnomalySeverity {
    NORMAL,
    UNUSUAL,
    VERY_UNUSUAL;

    val label: String
        get() = when (this) {
            NORMAL -> "Normal"
            UNUSUAL -> "Unusual"
            VERY_UNUSUAL -> "Very unusual"
        }
}

data class MetricStats(
    val metric: MetricType,
    val mean: Double,
    val median: Double,
    val stdDev: Double,
    val min: Double,
    val max: Double,
    val normalLow: Double,
    val normalHigh: Double,
    val count: Int
)

data class AnomalyRecord(
    val id: String,
    val date: String,
    val metric: MetricType,
    val observedValue: Double,
    val baselineMean: Double,
    val baselineMedian: Double,
    val normalRangeLow: Double,
    val normalRangeHigh: Double,
    val zScore: Double,
    val percentageDifference: Double,
    val severity: AnomalySeverity,
    val isHigherThanNormal: Boolean,
    val explanation: String
)

data class CorrelationResult(
    val metricA: MetricType,
    val metricB: MetricType,
    val r: Double,
    val sampleCount: Int,
    val description: String,
    val isPositive: Boolean
)

data class PatternDashboardState(
    val patternScore: Int = 0,
    val unusualCountThisWeek: Int = 0,
    val todayEntry: DailyEntry? = null,
    val recentAnomalies: List<AnomalyRecord> = emptyList(),
    val statsMap: Map<MetricType, MetricStats> = emptyMap(),
    val topCorrelations: List<CorrelationResult> = emptyList()
)
