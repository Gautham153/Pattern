package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.PatternAnalytics
import com.example.data.model.AnomalySeverity
import com.example.data.model.DailyEntry
import com.example.data.model.MetricType
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AnomalyOrange
import com.example.ui.theme.AnomalyRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PatternUiState

@Composable
fun HistoryScreen(
    uiState: PatternUiState,
    onSearchChange: (String) -> Unit,
    onToggleAnomaliesOnly: () -> Unit,
    onEditEntry: (DailyEntry) -> Unit,
    onAddNewDay: () -> Unit
) {
    val allEntries = uiState.allEntries
    val allAnomalies = uiState.allAnomalies

    // Filtered list based on search and anomalies-only toggle
    val filteredEntries = remember(allEntries, allAnomalies, uiState.historySearchQuery, uiState.historyAnomaliesOnly) {
        allEntries.filter { entry ->
            val matchesQuery = if (uiState.historySearchQuery.isBlank()) true
            else entry.date.contains(uiState.historySearchQuery, ignoreCase = true) ||
                    entry.formattedDateShort().contains(uiState.historySearchQuery, ignoreCase = true) ||
                    entry.notes.contains(uiState.historySearchQuery, ignoreCase = true)

            if (!matchesQuery) return@filter false

            if (uiState.historyAnomaliesOnly) {
                allAnomalies.any { it.date == entry.date }
            } else {
                true
            }
        }
    }

    var showSearchBar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .testTag("history_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showSearchBar = !showSearchBar },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (showSearchBar) AccentTeal else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Filter button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (uiState.historyAnomaliesOnly) PrimaryBlue else CardBackground)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleAnomaliesOnly() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (uiState.historyAnomaliesOnly) TextPrimary else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.historyAnomaliesOnly) "Anomalies" else "All Data",
                            color = if (uiState.historyAnomaliesOnly) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (showSearchBar) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = uiState.historySearchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by date or notes...", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Empty state check
        if (allEntries.isEmpty() || filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Empty",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (allEntries.isEmpty()) "No data yet" else "No matching entries",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (allEntries.isEmpty()) {
                            "Start logging your daily data to see your patterns and insights."
                        } else {
                            "Try clearing your search query or adjusting your filters."
                        },
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onAddNewDay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("add_first_day_button")
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (allEntries.isEmpty()) "Add First Day" else "Log New Day",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // Horizontal scroll container for the analytics table
            val horizontalScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF162030))
                            .horizontalScroll(horizontalScrollState)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell("Date", 64.dp)
                        TableHeaderCell("Sleep", 50.dp)
                        TableHeaderCell("Screen", 52.dp)
                        TableHeaderCell("Study", 50.dp)
                        TableHeaderCell("Exercise", 56.dp)
                        TableHeaderCell("Mood", 46.dp)
                        TableHeaderCell("Prod.", 46.dp)
                        TableHeaderCell("Anomaly", 60.dp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorder)
                    )

                    // Table Rows
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        items(filteredEntries, key = { it.date }) { entry ->
                            val anomalies = allAnomalies.filter { it.date == entry.date }
                            val highestSeverity = anomalies.maxOfOrNull { it.severity } ?: AnomalySeverity.NORMAL

                            Row(
                                modifier = Modifier
                                    .testTag("history_row_${entry.date}")
                                    .fillMaxWidth()
                                    .clickable { onEditEntry(entry) }
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Date Cell
                                Text(
                                    text = entry.formattedDateShort(),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(64.dp)
                                )

                                // Metrics Cells
                                MetricTableCell(
                                    value = String.format("%.1f", entry.sleepHours),
                                    isAnomaly = anomalies.any { it.metric == MetricType.SLEEP },
                                    width = 50.dp
                                )
                                MetricTableCell(
                                    value = String.format("%.1f", entry.screenTimeHours),
                                    isAnomaly = anomalies.any { it.metric == MetricType.SCREEN_TIME },
                                    width = 52.dp
                                )
                                MetricTableCell(
                                    value = String.format("%.1f", entry.studyHours),
                                    isAnomaly = anomalies.any { it.metric == MetricType.STUDY },
                                    width = 50.dp
                                )
                                MetricTableCell(
                                    value = "${entry.exerciseMinutes.toInt()}",
                                    isAnomaly = anomalies.any { it.metric == MetricType.EXERCISE },
                                    width = 56.dp
                                )
                                MetricTableCell(
                                    value = "${entry.mood.toInt()}",
                                    isAnomaly = anomalies.any { it.metric == MetricType.MOOD },
                                    width = 46.dp
                                )
                                MetricTableCell(
                                    value = "${entry.productivity.toInt()}",
                                    isAnomaly = anomalies.any { it.metric == MetricType.PRODUCTIVITY },
                                    width = 46.dp
                                )

                                // Anomaly Indicator Dot / Dash Cell
                                Box(
                                    modifier = Modifier.width(60.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    when (highestSeverity) {
                                        AnomalySeverity.VERY_UNUSUAL -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(AnomalyRed, CircleShape)
                                            )
                                        }
                                        AnomalySeverity.UNUSUAL -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(AnomalyOrange, CircleShape)
                                            )
                                        }
                                        AnomalySeverity.NORMAL -> {
                                            Text(text = "—", color = TextMuted, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(CardBorder.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(title: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun MetricTableCell(
    value: String,
    isAnomaly: Boolean,
    width: androidx.compose.ui.unit.Dp
) {
    Text(
        text = value,
        color = if (isAnomaly) AnomalyRed else TextSecondary,
        fontSize = 12.sp,
        fontWeight = if (isAnomaly) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.width(width)
    )
}
