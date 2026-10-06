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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyEntry
import com.example.data.model.MetricType
import com.example.ui.components.TrendLineChart
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PatternUiState
import com.example.ui.viewmodel.TrendRange

@Composable
fun TrendsScreen(
    uiState: PatternUiState,
    onMetricSelect: (MetricType) -> Unit,
    onRangeSelect: (TrendRange) -> Unit,
    onPointClicked: (DailyEntry) -> Unit
) {
    var showMetricMenu by remember { mutableStateOf(false) }

    val filteredEntries = remember(uiState.allEntries, uiState.selectedTrendRange) {
        uiState.allEntries.take(uiState.selectedTrendRange.days)
    }

    val stats = uiState.statsMap[uiState.selectedTrendMetric]

    Column(
        modifier = Modifier
            .testTag("trends_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Screen Header
        Text(
            text = "Trends",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Top Filter Bar: Metric Dropdown on Left | Range Pills on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Metric Dropdown Selector
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .clickable { showMetricMenu = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = uiState.selectedTrendMetric.icon,
                        contentDescription = "Metric",
                        tint = AccentTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.selectedTrendMetric.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMetricMenu,
                    onDismissRequest = { showMetricMenu = false }
                ) {
                    MetricType.entries.forEach { metric ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = metric.icon,
                                        contentDescription = metric.title,
                                        tint = if (metric == uiState.selectedTrendMetric) AccentTeal else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = metric.title,
                                        fontWeight = if (metric == uiState.selectedTrendMetric) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            trailingIcon = if (metric == uiState.selectedTrendMetric) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = AccentTeal) }
                            } else null,
                            onClick = {
                                onMetricSelect(metric)
                                showMetricMenu = false
                            }
                        )
                    }
                }
            }

            // Time Range Switcher (7 days | 14 days | 30 days)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                TrendRange.entries.forEach { range ->
                    val isSelected = uiState.selectedTrendRange == range
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) PrimaryBlue else CardBackground)
                            .clickable { onRangeSelect(range) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = range.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Custom Trend Line Chart
        TrendLineChart(
            metric = uiState.selectedTrendMetric,
            entries = filteredEntries,
            stats = stats,
            anomalies = uiState.allAnomalies,
            onPointClicked = onPointClicked
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Metric Switcher Bar (6 Icons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricType.entries.forEach { metric ->
                val isSelected = metric == uiState.selectedTrendMetric
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMetricSelect(metric) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryBlue else CardBackground)
                            .border(1.dp, if (isSelected) AccentTeal else CardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = metric.icon,
                            contentDescription = metric.title,
                            tint = if (isSelected) TextPrimary else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = metric.shortTitle,
                        color = if (isSelected) TextPrimary else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Statistical Baseline Summary for Selected Metric
        if (stats != null && stats.count > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "${uiState.selectedTrendMetric.title} Baseline Insights",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Average", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = uiState.selectedTrendMetric.formatValue(stats.mean),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column {
                            Text(text = "Median", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = uiState.selectedTrendMetric.formatValue(stats.median),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column {
                            Text(text = "Normal Range", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${uiState.selectedTrendMetric.formatCompact(stats.normalLow)} – ${uiState.selectedTrendMetric.formatCompact(stats.normalHigh)}",
                                color = AccentTeal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
