package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AnomalyRecord
import com.example.data.model.AnomalySeverity
import com.example.data.model.DailyEntry
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AnomalyOrange
import com.example.ui.theme.AnomalyRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AnomalyDetailDialog(
    anomaly: AnomalyRecord,
    allEntries: List<DailyEntry>,
    onDismiss: () -> Unit,
    onEditEntry: (DailyEntry) -> Unit
) {
    val matchingEntry = allEntries.find { it.date == anomaly.date }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .testTag("anomaly_detail_dialog")
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Anomaly Details",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Severity Banner
                AnomalyTag(
                    severity = anomaly.severity,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Metric Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentTeal.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = anomaly.metric.icon,
                            contentDescription = anomaly.metric.title,
                            tint = AccentTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = anomaly.metric.title,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = anomaly.date,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Comparison Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Observed value", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = anomaly.metric.formatValue(anomaly.observedValue),
                                color = if (anomaly.severity == AnomalySeverity.VERY_UNUSUAL) AnomalyRed else AnomalyOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Your normal range", color = TextSecondary, fontSize = 13.sp)
                            val lowFormatted = anomaly.metric.formatCompact(anomaly.normalRangeLow)
                            val highFormatted = anomaly.metric.formatValue(anomaly.normalRangeHigh)
                            Text(
                                text = "$lowFormatted – $highFormatted",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Difference", color = TextSecondary, fontSize = 13.sp)
                            val prefix = if (anomaly.percentageDifference > 0) "+" else ""
                            Text(
                                text = "$prefix${String.format("%.0f", anomaly.percentageDifference)}%",
                                color = if (anomaly.percentageDifference > 0) AnomalyRed else AnomalyOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanation Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlue.copy(alpha = 0.08f))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = AccentTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = anomaly.explanation,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Recent Values
                Text(
                    text = "Recent Values for ${anomaly.metric.title}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val recentEntriesForMetric = allEntries
                    .sortedByDescending { it.date }
                    .take(4)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                ) {
                    recentEntriesForMetric.forEachIndexed { index, entry ->
                        val entryVal = entry.getMetricValue(anomaly.metric)
                        val isThisAnomaly = entry.date == anomaly.date

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.formattedDateFull(),
                                color = if (isThisAnomaly) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isThisAnomaly) FontWeight.Bold else FontWeight.Normal
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = anomaly.metric.formatValue(entryVal),
                                    color = if (isThisAnomaly) AnomalyRed else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (isThisAnomaly) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(observed)",
                                        color = AnomalyRed,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        if (index < recentEntriesForMetric.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(CardBorder)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (matchingEntry != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onEditEntry(matchingEntry)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .testTag("anomaly_edit_day_button")
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Edit Day Log", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardBackground,
                            contentColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .testTag("anomaly_close_button")
                            .weight(1f)
                            .height(46.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Text("Close", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
