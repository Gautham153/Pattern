package com.example.data.analytics

import com.example.data.model.AnomalyRecord
import com.example.data.model.AnomalySeverity
import com.example.data.model.CorrelationResult
import com.example.data.model.DailyEntry
import com.example.data.model.MetricStats
import com.example.data.model.MetricType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class AnomalyDetectionResult(
    val severity: AnomalySeverity,
    val zScore: Double,
    val percentageDifference: Double,
    val baselineMean: Double,
    val baselineMedian: Double,
    val baselineStdDev: Double,
    val isFallback: Boolean,
    val isHigher: Boolean
)

object PatternAnalytics {

    // Threshold constants as strictly defined in specification:
    // Normal: |z| < 2.0
    // Unusual: 2.0 <= |z| < 2.8
    // Very unusual: |z| >= 2.8
    const val Z_THRESHOLD_UNUSUAL = 2.0
    const val Z_THRESHOLD_VERY_UNUSUAL = 2.8

    // Fallback percentage thresholds when baseline history has < 7 observations or zero variance:
    const val FALLBACK_PCT_UNUSUAL = 35.0
    const val FALLBACK_PCT_VERY_UNUSUAL = 65.0

    // Minimum history size for standard parametric z-score analysis
    const val MIN_HISTORY_FOR_Z_SCORE = 7

    fun calculateMean(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        return values.sum() / values.size
    }

    fun calculateMedian(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val size = sorted.size
        return if (size % 2 == 1) {
            sorted[size / 2]
        } else {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
        }
    }

    fun calculateStandardDeviation(values: List<Double>, mean: Double = calculateMean(values)): Double {
        if (values.size <= 1) return 0.0
        val sumSquaredDiff = values.sumOf { (it - mean) * (it - mean) }
        val variance = sumSquaredDiff / (values.size - 1)
        if (variance <= 0.0 || variance.isNaN() || variance.isInfinite()) return 0.0
        return sqrt(variance)
    }

    fun calculateZScore(value: Double, mean: Double, stdDev: Double): Double {
        if (stdDev <= 1e-6 || stdDev.isNaN() || stdDev.isInfinite()) return 0.0
        val z = (value - mean) / stdDev
        if (z.isNaN() || z.isInfinite()) return 0.0
        return z
    }

    /**
     * Evaluates whether [value] is anomalous compared against the provided [baselineValues].
     * Note: The evaluated observation MUST NOT be in [baselineValues].
     */
    fun detectAnomaly(value: Double, baselineValues: List<Double>): AnomalyDetectionResult {
        if (baselineValues.isEmpty()) {
            return AnomalyDetectionResult(
                severity = AnomalySeverity.NORMAL,
                zScore = 0.0,
                percentageDifference = 0.0,
                baselineMean = value,
                baselineMedian = value,
                baselineStdDev = 0.0,
                isFallback = true,
                isHigher = false
            )
        }

        val mean = calculateMean(baselineValues)
        val median = calculateMedian(baselineValues)
        val isHigher = value > mean

        // Edge case: fewer than 7 historical observations -> use percentage deviation fallback
        if (baselineValues.size < MIN_HISTORY_FOR_Z_SCORE) {
            val pctDiff = if (mean > 1e-6) {
                ((value - mean) / mean) * 100.0
            } else if (value == 0.0) {
                0.0
            } else {
                100.0
            }
            val absPct = abs(pctDiff)

            val severity = when {
                absPct >= FALLBACK_PCT_VERY_UNUSUAL -> AnomalySeverity.VERY_UNUSUAL
                absPct >= FALLBACK_PCT_UNUSUAL -> AnomalySeverity.UNUSUAL
                else -> AnomalySeverity.NORMAL
            }

            return AnomalyDetectionResult(
                severity = severity,
                zScore = 0.0,
                percentageDifference = pctDiff,
                baselineMean = mean,
                baselineMedian = median,
                baselineStdDev = 0.0,
                isFallback = true,
                isHigher = isHigher
            )
        }

        // 7 or more historical observations: calculate sample standard deviation
        val stdDev = calculateStandardDeviation(baselineValues, mean)

        // Edge case: standard deviation is zero (all historical values are identical)
        if (stdDev <= 1e-6) {
            if (abs(value - mean) <= 1e-6) {
                return AnomalyDetectionResult(
                    severity = AnomalySeverity.NORMAL,
                    zScore = 0.0,
                    percentageDifference = 0.0,
                    baselineMean = mean,
                    baselineMedian = median,
                    baselineStdDev = 0.0,
                    isFallback = false,
                    isHigher = false
                )
            }

            // Values differ from identical baseline: fallback to percentage difference
            val pctDiff = if (mean > 1e-6) {
                ((value - mean) / mean) * 100.0
            } else {
                100.0
            }
            val absPct = abs(pctDiff)

            val severity = when {
                absPct >= FALLBACK_PCT_VERY_UNUSUAL -> AnomalySeverity.VERY_UNUSUAL
                absPct >= FALLBACK_PCT_UNUSUAL -> AnomalySeverity.UNUSUAL
                else -> AnomalySeverity.NORMAL
            }

            return AnomalyDetectionResult(
                severity = severity,
                zScore = 0.0,
                percentageDifference = pctDiff,
                baselineMean = mean,
                baselineMedian = median,
                baselineStdDev = 0.0,
                isFallback = true,
                isHigher = isHigher
            )
        }

        // Standard z-score calculation
        val rawZ = calculateZScore(value, mean, stdDev)
        val absZ = abs(rawZ)
        val pctDiff = if (mean > 1e-6) ((value - mean) / mean) * 100.0 else 0.0

        val severity = when {
            absZ >= Z_THRESHOLD_VERY_UNUSUAL -> AnomalySeverity.VERY_UNUSUAL
            absZ >= Z_THRESHOLD_UNUSUAL -> AnomalySeverity.UNUSUAL
            else -> AnomalySeverity.NORMAL
        }

        return AnomalyDetectionResult(
            severity = severity,
            zScore = rawZ,
            percentageDifference = pctDiff,
            baselineMean = mean,
            baselineMedian = median,
            baselineStdDev = stdDev,
            isFallback = false,
            isHigher = isHigher
        )
    }

    /**
     * Evaluates anomalies for a given [entry] against [allEntries].
     * Guarantees that [entry] is excluded from its own baseline calculation.
     */
    fun detectAnomaliesForEntry(
        entry: DailyEntry,
        allEntries: List<DailyEntry>
    ): List<AnomalyRecord> {
        val anomalies = mutableListOf<AnomalyRecord>()
        // CRITICAL: Exclude this specific entry from its baseline
        val baselineEntries = allEntries.filter { it.date != entry.date }

        for (metric in MetricType.entries) {
            val baselineValues = baselineEntries.map { it.getMetricValue(metric) }
            val value = entry.getMetricValue(metric)
            val result = detectAnomaly(value, baselineValues)

            if (result.severity != AnomalySeverity.NORMAL) {
                val effectiveStdDev = if (result.baselineStdDev > 1e-6) result.baselineStdDev else (result.baselineMean * 0.15).coerceAtLeast(0.5)
                val normalRangeLow = max(metric.minAllowed, result.baselineMean - 2.0 * effectiveStdDev)
                val normalRangeHigh = min(metric.maxAllowed, result.baselineMean + 2.0 * effectiveStdDev)

                val formattedObs = metric.formatValue(value)
                val formattedMean = metric.formatValue(result.baselineMean)
                val explanation = if (result.isHigher) {
                    "Your ${metric.title.lowercase()} ($formattedObs) was noticeably higher than your baseline ($formattedMean)."
                } else {
                    "Your ${metric.title.lowercase()} ($formattedObs) was noticeably lower than your baseline ($formattedMean)."
                }

                anomalies.add(
                    AnomalyRecord(
                        id = "${entry.date}_${metric.id}",
                        date = entry.date,
                        metric = metric,
                        observedValue = value,
                        baselineMean = result.baselineMean,
                        baselineMedian = result.baselineMedian,
                        normalRangeLow = normalRangeLow,
                        normalRangeHigh = normalRangeHigh,
                        zScore = result.zScore,
                        percentageDifference = result.percentageDifference,
                        severity = result.severity,
                        isHigherThanNormal = result.isHigher,
                        explanation = explanation
                    )
                )
            }
        }
        return anomalies
    }

    /**
     * Evaluates a single metric value against an explicit list of baseline entries (which already excludes the target day).
     */
    fun detectAnomalyForMetricValue(
        value: Double,
        metric: MetricType,
        baselineEntries: List<DailyEntry>
    ): Pair<AnomalySeverity, Double> {
        val baselineValues = baselineEntries.map { it.getMetricValue(metric) }
        val result = detectAnomaly(value, baselineValues)
        return Pair(result.severity, result.percentageDifference)
    }

    /**
     * Computes all anomalies across [entries], evaluating each day against all other days.
     */
    fun getAllAnomalies(entries: List<DailyEntry>): List<AnomalyRecord> {
        if (entries.isEmpty()) return emptyList()
        val sorted = entries.sortedByDescending { it.date }
        return sorted.flatMap { detectAnomaliesForEntry(it, entries) }
    }

    /**
     * Global baseline statistics over the entire dataset (used for overall profile and Insights display).
     */
    fun calculateStats(metric: MetricType, entries: List<DailyEntry>): MetricStats {
        val values = entries.map { it.getMetricValue(metric) }
        if (values.isEmpty()) {
            return MetricStats(
                metric = metric,
                mean = 0.0,
                median = 0.0,
                stdDev = 0.0,
                min = 0.0,
                max = 0.0,
                normalLow = 0.0,
                normalHigh = 0.0,
                count = 0
            )
        }

        val mean = calculateMean(values)
        val median = calculateMedian(values)
        val stdDev = calculateStandardDeviation(values, mean)
        val minVal = values.minOrNull() ?: 0.0
        val maxVal = values.maxOrNull() ?: 0.0

        val effectiveStdDev = if (stdDev > 1e-6) stdDev else (mean * 0.15).coerceAtLeast(0.5)
        val rawLow = mean - 2.0 * effectiveStdDev
        val rawHigh = mean + 2.0 * effectiveStdDev

        val normalLow = max(metric.minAllowed, String.format(java.util.Locale.US, "%.1f", rawLow).toDouble())
        val normalHigh = min(metric.maxAllowed, String.format(java.util.Locale.US, "%.1f", rawHigh).toDouble())

        return MetricStats(
            metric = metric,
            mean = mean,
            median = median,
            stdDev = stdDev,
            min = minVal,
            max = maxVal,
            normalLow = normalLow,
            normalHigh = normalHigh,
            count = values.size
        )
    }

    fun calculateAllStats(entries: List<DailyEntry>): Map<MetricType, MetricStats> {
        return MetricType.entries.associateWith { calculateStats(it, entries) }
    }

    /**
     * Calculates the Pearson correlation coefficient between [x] and [y].
     * Safeguards against zero variance and returns a value strictly bounded in [-1.0, 1.0].
     */
    fun calculatePearsonCorrelation(x: List<Double>, y: List<Double>): Double {
        if (x.size != y.size || x.size < 5) return 0.0

        val meanX = calculateMean(x)
        val meanY = calculateMean(y)

        var numerator = 0.0
        var denomX = 0.0
        var denomY = 0.0

        for (i in x.indices) {
            val dx = x[i] - meanX
            val dy = y[i] - meanY
            numerator += dx * dy
            denomX += dx * dx
            denomY += dy * dy
        }

        if (denomX <= 1e-9 || denomY <= 1e-9) return 0.0
        val denominator = sqrt(denomX * denomY)
        if (denominator.isNaN() || denominator.isInfinite() || denominator <= 1e-9) return 0.0

        val r = numerator / denominator
        if (r.isNaN() || r.isInfinite()) return 0.0
        return r.coerceIn(-1.0, 1.0)
    }

    fun generateCorrelations(entries: List<DailyEntry>): List<CorrelationResult> {
        if (entries.size < 5) return emptyList()

        val pairs = listOf(
            Pair(MetricType.SLEEP, MetricType.PRODUCTIVITY),
            Pair(MetricType.SCREEN_TIME, MetricType.PRODUCTIVITY),
            Pair(MetricType.EXERCISE, MetricType.MOOD),
            Pair(MetricType.STUDY, MetricType.PRODUCTIVITY),
            Pair(MetricType.SLEEP, MetricType.MOOD),
            Pair(MetricType.SCREEN_TIME, MetricType.SLEEP)
        )

        val results = mutableListOf<CorrelationResult>()

        for ((metricA, metricB) in pairs) {
            val listA = entries.map { it.getMetricValue(metricA) }
            val listB = entries.map { it.getMetricValue(metricB) }
            val r = calculatePearsonCorrelation(listA, listB)

            if (abs(r) >= 0.25) {
                val isPositive = r > 0
                val description = when {
                    metricA == MetricType.SLEEP && metricB == MetricType.PRODUCTIVITY ->
                        if (isPositive) "Your data shows a positive relationship between sleep and productivity."
                        else "Your data shows lower productivity on days with higher recorded sleep."

                    metricA == MetricType.SCREEN_TIME && metricB == MetricType.PRODUCTIVITY ->
                        if (!isPositive) "Your data shows a negative relationship between screen time and productivity."
                        else "Your data shows a positive association between screen time and productivity."

                    metricA == MetricType.EXERCISE && metricB == MetricType.MOOD ->
                        if (isPositive) "Your data shows a positive relationship between exercise and mood."
                        else "Your data shows an inverse relationship between exercise and mood."

                    metricA == MetricType.STUDY && metricB == MetricType.PRODUCTIVITY ->
                        if (isPositive) "Your data shows a positive relationship between study hours and productivity."
                        else "Your data shows an inverse relationship between study hours and productivity."

                    metricA == MetricType.SLEEP && metricB == MetricType.MOOD ->
                        if (isPositive) "Your data shows a positive relationship between sleep duration and mood."
                        else "Your data shows an inverse relationship between sleep duration and mood."

                    metricA == MetricType.SCREEN_TIME && metricB == MetricType.SLEEP ->
                        if (!isPositive) "Your data shows a negative relationship between screen time and sleep duration."
                        else "Your data shows screen time and sleep moving in the same direction."

                    else -> if (isPositive) "Your data shows a positive relationship between ${metricA.title} and ${metricB.title}."
                    else "Your data shows a negative relationship between ${metricA.title} and ${metricB.title}."
                }

                results.add(
                    CorrelationResult(
                        metricA = metricA,
                        metricB = metricB,
                        r = r,
                        sampleCount = entries.size,
                        description = description,
                        isPositive = isPositive
                    )
                )
            }
        }

        return results.sortedByDescending { abs(it.r) }
    }

    /**
     * Calculates the Pattern Score (0–100) representing consistency against personal baseline.
     * Evaluates observations in [recentEntries] against baseline from other historical entries.
     * - Normal observation: 1.0 point
     * - Unusual observation: 0.5 points (reduces consistency)
     * - Very unusual observation: 0.0 points (reduces consistency more)
     */
    fun calculatePatternScore(
        recentEntries: List<DailyEntry>,
        allEntries: List<DailyEntry>
    ): Int {
        if (allEntries.isEmpty()) return 100
        val targetEntries = if (recentEntries.isNotEmpty()) recentEntries else allEntries.take(7)
        if (targetEntries.isEmpty()) return 100

        var totalObservations = 0
        var consistencyPoints = 0.0

        for (entry in targetEntries) {
            val baselineEntries = allEntries.filter { it.date != entry.date }
            for (metric in MetricType.entries) {
                val baselineValues = baselineEntries.map { it.getMetricValue(metric) }
                val anomalyResult = detectAnomaly(entry.getMetricValue(metric), baselineValues)
                totalObservations++
                when (anomalyResult.severity) {
                    AnomalySeverity.NORMAL -> consistencyPoints += 1.0
                    AnomalySeverity.UNUSUAL -> consistencyPoints += 0.5
                    AnomalySeverity.VERY_UNUSUAL -> consistencyPoints += 0.0
                }
            }
        }

        if (totalObservations == 0) return 100
        val rawScore = (consistencyPoints / totalObservations) * 100.0
        return rawScore.roundToInt().coerceIn(0, 100)
    }
}
