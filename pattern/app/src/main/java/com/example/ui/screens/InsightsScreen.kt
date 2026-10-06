package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnomalyRecord
import com.example.data.model.CorrelationResult
import com.example.data.model.MetricStats
import com.example.data.model.MetricType
import com.example.ui.components.AnomalyTag
import com.example.ui.components.BaselineMetricCard
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AnomalyOrange
import com.example.ui.theme.AnomalyRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.NormalGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InsightsTab
import com.example.ui.viewmodel.PatternUiState

@Composable
fun InsightsScreen(
    uiState: PatternUiState,
    onTabSelected: (InsightsTab) -> Unit,
    onSelectAnomaly: (AnomalyRecord) -> Unit,
    onSelectMetric: (MetricType) -> Unit
) {
    Column(
        modifier = Modifier
            .testTag("insights_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Screen Header
        Text(
            text = "Insights",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Control Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            InsightsTab.entries.forEach { tab ->
                val isSelected = uiState.selectedInsightsTab == tab
                val tabLabel = when (tab) {
                    InsightsTab.OVERVIEW -> "Overview"
                    InsightsTab.BASELINE -> "Baseline"
                    InsightsTab.CORRELATIONS -> "Correlations"
                }

                Box(
                    modifier = Modifier
                        .testTag("insights_tab_${tab.name.lowercase()}")
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PrimaryBlue else CardBackground)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLabel,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (uiState.selectedInsightsTab) {
            InsightsTab.OVERVIEW -> OverviewTabContent(
                uiState = uiState,
                onSelectAnomaly = onSelectAnomaly,
                onSelectMetric = onSelectMetric
            )
            InsightsTab.BASELINE -> BaselineTabContent(
                statsMap = uiState.statsMap,
                entryCount = uiState.allEntries.size,
                onSelectMetric = onSelectMetric
            )
            InsightsTab.CORRELATIONS -> CorrelationsTabContent(
                correlations = uiState.correlations,
                entryCount = uiState.allEntries.size
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun OverviewTabContent(
    uiState: PatternUiState,
    onSelectAnomaly: (AnomalyRecord) -> Unit,
    onSelectMetric: (MetricType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section: Your Personal Baseline
        Text(
            text = "Your Personal Baseline",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Average and middle values from your past data.",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6 Baseline Cards (2 Columns)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BaselineMetricCard(
                    metric = MetricType.SLEEP,
                    stats = uiState.statsMap[MetricType.SLEEP],
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMetric(MetricType.SLEEP) }
                )
                BaselineMetricCard(
                    metric = MetricType.SCREEN_TIME,
                    stats = uiState.statsMap[MetricType.SCREEN_TIME],
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMetric(MetricType.SCREEN_TIME) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BaselineMetricCard(
                    metric = MetricType.STUDY,
                    stats = uiState.statsMap[MetricType.STUDY],
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMetric(MetricType.STUDY) }
                )
                BaselineMetricCard(
                    metric = MetricType.EXERCISE,
                    stats = uiState.statsMap[MetricType.EXERCISE],
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMetric(MetricType.EXERCISE) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BaselineMetricCard(
                    metric = MetricType.MOOD,
                    stats = uiState.statsMap[MetricType.MOOD],
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMetric(MetricType.MOOD) }
                )
                BaselineMetricCard(
                    metric = MetricType.PRODUCTIVITY,
                    stats = uiState.statsMap[MetricType.PRODUCTIVITY],
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectMetric(MetricType.PRODUCTIVITY) }
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Section: Recent Anomalies
        Text(
            text = "Recent Anomalies",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.recentAnomalies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No anomalies detected in your recent logs.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentAnomalies.forEach { anomaly ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelectAnomaly(anomaly) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = anomaly.date,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = anomaly.metric.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Observed: ${anomaly.metric.formatValue(anomaly.observedValue)} • Avg: ${anomaly.metric.formatCompact(anomaly.baselineMean)}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            AnomalyTag(severity = anomaly.severity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BaselineTabContent(
    statsMap: Map<MetricType, MetricStats>,
    entryCount: Int,
    onSelectMetric: (MetricType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryBlue.copy(alpha = 0.08f))
                .border(1.dp, PrimaryBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = AccentTeal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personal baselines are derived from $entryCount logged days. Values exceeding 2 standard deviations (|z| ≥ 2.0) are categorized as unusual.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricType.entries.forEach { metric ->
                val stats = statsMap[metric]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .clickable { onSelectMetric(metric) }
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(AccentTeal.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = metric.icon,
                                        contentDescription = metric.title,
                                        tint = AccentTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = metric.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Normal: ${metric.formatCompact(stats?.normalLow ?: 0.0)} – ${metric.formatValue(stats?.normalHigh ?: 0.0)}",
                                color = AccentTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Grid: Mean | Median | Std Dev | Min | Max
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniItem("Mean", if (stats != null) metric.formatCompact(stats.mean) else "—")
                            StatMiniItem("Median", if (stats != null) metric.formatCompact(stats.median) else "—")
                            StatMiniItem("Std Dev", if (stats != null) String.format("%.2f", stats.stdDev) else "—")
                            StatMiniItem("Min", if (stats != null) metric.formatCompact(stats.min) else "—")
                            StatMiniItem("Max", if (stats != null) metric.formatCompact(stats.max) else "—")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CorrelationsTabContent(
    correlations: List<CorrelationResult>,
    entryCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Metric Relationships",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Pearson statistical correlations across $entryCount logged days.",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (correlations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log at least 5 days to generate statistical correlations.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                correlations.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (item.isPositive) NormalGreen.copy(alpha = 0.15f)
                                        else AnomalyOrange.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isPositive) Icons.AutoMirrored.Filled.TrendingUp
                                    else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = "Trend",
                                    tint = if (item.isPositive) NormalGreen else AnomalyOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.metricA.title} & ${item.metricB.title}",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "r = ${String.format("%+.2f", item.r)}",
                                        color = if (item.isPositive) NormalGreen else AnomalyOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
