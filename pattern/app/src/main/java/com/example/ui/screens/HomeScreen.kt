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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnomalyRecord
import com.example.data.model.DailyEntry
import com.example.data.model.MetricType
import com.example.ui.components.AnomalyTag
import com.example.ui.components.MetricSummaryCard
import com.example.ui.components.PatternScoreRing
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AnomalyAmber
import com.example.ui.theme.AnomalyOrange
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.NormalGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PatternUiState

@Composable
fun HomeScreen(
    uiState: PatternUiState,
    onLogToday: () -> Unit,
    onEditToday: (DailyEntry) -> Unit,
    onViewAnomalies: () -> Unit,
    onSelectAnomaly: (AnomalyRecord) -> Unit,
    onNavigateTrends: (MetricType) -> Unit,
    onResetDemoData: () -> Unit,
    onClearAllData: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .testTag("home_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // App Header: Logo + App Name + Actions Menu & User Avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3 Vertical Bars Logo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(11.dp)
                            .background(AccentTeal, RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(20.dp)
                            .background(PrimaryBlue, RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(15.dp)
                            .background(AccentCyan, RoundedCornerShape(2.dp))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Pattern",
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reset to 28-day Sample Data") },
                            onClick = {
                                showMenu = false
                                onResetDemoData()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Logged Data") },
                            onClick = {
                                showMenu = false
                                onClearAllData()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Avatar Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentTeal.copy(alpha = 0.2f))
                        .border(1.dp, AccentTeal.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.userName.take(1).uppercase(),
                        color = AccentTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Greeting & Current Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Good morning, ${uiState.userName}",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Here's your pattern overview for today.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Text(
                text = DailyEntry.todayDateString(),
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pattern Score Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PatternScoreRing(
                        score = uiState.patternScore,
                        size = 80.dp,
                        strokeWidth = 7.dp
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pattern Score",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "How consistent your recent data is with your personal baseline.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unusual patterns banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (uiState.weeklyAnomaliesCount > 0) AnomalyAmber.copy(alpha = 0.12f)
                            else NormalGreen.copy(alpha = 0.1f)
                        )
                        .border(
                            1.dp,
                            if (uiState.weeklyAnomaliesCount > 0) AnomalyAmber.copy(alpha = 0.3f)
                            else NormalGreen.copy(alpha = 0.25f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onViewAnomalies)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Alert",
                                tint = if (uiState.weeklyAnomaliesCount > 0) AnomalyOrange else NormalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.weeklyAnomaliesCount > 0) {
                                    if (uiState.weeklyAnomaliesCount == 1) "1 unusual pattern detected this week"
                                    else "${uiState.weeklyAnomaliesCount} unusual patterns detected this week"
                                } else {
                                    "No unusual anomalies detected recently"
                                },
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Summary Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Summary",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (uiState.todayEntry != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onEditToday(uiState.todayEntry) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = AccentTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit Today",
                        color = AccentTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Button(
                    onClick = onLogToday,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .testTag("log_today_quick_button")
                        .height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Today's 6 Metric Cards (3x2 Grid)
        val today = uiState.todayEntry
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Sleep, Screen Time, Study
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    metric = MetricType.SLEEP,
                    value = today?.sleepHours,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTrends(MetricType.SLEEP) }
                )
                MetricSummaryCard(
                    metric = MetricType.SCREEN_TIME,
                    value = today?.screenTimeHours,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTrends(MetricType.SCREEN_TIME) }
                )
                MetricSummaryCard(
                    metric = MetricType.STUDY,
                    value = today?.studyHours,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTrends(MetricType.STUDY) }
                )
            }

            // Row 2: Exercise, Mood, Productivity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    metric = MetricType.EXERCISE,
                    value = today?.exerciseMinutes,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTrends(MetricType.EXERCISE) }
                )
                MetricSummaryCard(
                    metric = MetricType.MOOD,
                    value = today?.mood,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTrends(MetricType.MOOD) }
                )
                MetricSummaryCard(
                    metric = MetricType.PRODUCTIVITY,
                    value = today?.productivity,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTrends(MetricType.PRODUCTIVITY) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Anomalies Peek
        if (uiState.recentAnomalies.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Anomalies",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View all →",
                    color = AccentTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(onClick = onViewAnomalies)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentAnomalies.take(3).forEach { anomaly ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = anomaly.date,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = anomaly.metric.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Observed: ${anomaly.metric.formatValue(anomaly.observedValue)} (normal: ${anomaly.metric.formatCompact(anomaly.normalRangeLow)} – ${anomaly.metric.formatCompact(anomaly.normalRangeHigh)})",
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

        Spacer(modifier = Modifier.height(24.dp))

        // Top Correlation Insight preview
        if (uiState.correlations.isNotEmpty()) {
            Text(
                text = "Key Data Relationship",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            val topCorr = uiState.correlations.first()
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
                                if (topCorr.isPositive) NormalGreen.copy(alpha = 0.15f)
                                else AnomalyOrange.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (topCorr.isPositive) Icons.AutoMirrored.Filled.TrendingUp
                            else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Trend",
                            tint = if (topCorr.isPositive) NormalGreen else AnomalyOrange,
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
                                text = "${topCorr.metricA.title} & ${topCorr.metricB.title}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "r = ${String.format("%+.2f", topCorr.r)}",
                                color = if (topCorr.isPositive) NormalGreen else AnomalyOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = topCorr.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
