package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.PatternAnalytics
import com.example.data.model.AnomalySeverity
import com.example.data.model.DailyEntry
import com.example.data.model.MetricType
import com.example.ui.components.AnomalyTag
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AnomalyOrange
import com.example.ui.theme.AnomalyRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LogFormState
import com.example.ui.viewmodel.PatternUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LogDayScreen(
    uiState: PatternUiState,
    formState: LogFormState,
    onMetricChange: (MetricType, Double) -> Unit,
    onDateChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    BackHandler {
        onCancel()
    }

    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val calendar = remember(formState.date) {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d = sdf.parse(formState.date)
            if (d != null) cal.time = d
        } catch (e: Exception) {
            // keep current time
        }
        cal
    }

    fun openDatePicker() {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateChange(formatted)
        }, y, m, d).show()
    }

    fun shiftDate(days: Int) {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d = sdf.parse(formState.date)
            if (d != null) cal.time = d
        } catch (e: Exception) {}
        cal.add(Calendar.DAY_OF_MONTH, days)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        onDateChange(sdf.format(cal.time))
    }

    val dateDisplayFormatted = remember(formState.date) {
        try {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val sdfOut = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val d = sdfIn.parse(formState.date)
            if (d != null) sdfOut.format(d) else formState.date
        } catch (e: Exception) {
            formState.date
        }
    }

    Column(
        modifier = Modifier
            .testTag("log_day_screen")
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Screen Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (formState.isEditing) "Edit Day" else "Log Day",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Date Picker Bar with Prev/Next buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { shiftDate(-1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Day",
                        tint = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { openDatePicker() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = AccentTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateDisplayFormatted,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { shiftDate(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Day",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section Title: Daily Metrics
        Text(
            text = "Daily Metrics",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 6 Metrics Inputs
        val baselineEntries = remember(uiState.allEntries, formState.date) {
            uiState.allEntries.filter { it.date != formState.date }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricType.entries.forEach { metric ->
                val currentValue = when (metric) {
                    MetricType.SLEEP -> formState.sleepHours
                    MetricType.SCREEN_TIME -> formState.screenTimeHours
                    MetricType.STUDY -> formState.studyHours
                    MetricType.EXERCISE -> formState.exerciseMinutes
                    MetricType.MOOD -> formState.mood
                    MetricType.PRODUCTIVITY -> formState.productivity
                }

                val (anomalySeverity, _) = PatternAnalytics.detectAnomalyForMetricValue(
                    value = currentValue,
                    metric = metric,
                    baselineEntries = baselineEntries
                )

                MetricInputRow(
                    metric = metric,
                    value = currentValue,
                    anomalySeverity = anomalySeverity,
                    onValueChange = { onMetricChange(metric, it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .testTag("save_day_button")
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (formState.isEditing) "Save Changes" else "Save Day",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (formState.isEditing) {
            Button(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = AnomalyRed
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .testTag("delete_day_button")
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = AnomalyRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Entry", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .testTag("cancel_log_button")
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry?", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete data for $dateDisplayFormatted?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AnomalyRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                ) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun MetricInputRow(
    metric: MetricType,
    value: Double,
    anomalySeverity: AnomalySeverity,
    onValueChange: (Double) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(
                1.dp,
                if (anomalySeverity != AnomalySeverity.NORMAL) AnomalyOrange.copy(alpha = 0.5f) else CardBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric icon & title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(AccentTeal.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = metric.icon,
                            contentDescription = metric.title,
                            tint = AccentTeal,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "${metric.title} (${metric.unit})",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Value Display & Range
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (anomalySeverity != AnomalySeverity.NORMAL) {
                        AnomalyTag(severity = anomalySeverity)
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = if (metric.isInteger) "${value.toInt()}" else String.format("%.1f", value),
                        color = if (anomalySeverity == AnomalySeverity.VERY_UNUSUAL) AnomalyRed else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "(${metric.minAllowed.toInt()}–${metric.maxAllowed.toInt()})",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Interactive Controls: Minus Button | Slider | Plus Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val next = (value - metric.defaultStep).coerceAtLeast(metric.minAllowed)
                        onValueChange(next)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Slider(
                    value = value.toFloat(),
                    onValueChange = { onValueChange(it.toDouble()) },
                    valueRange = metric.minAllowed.toFloat()..metric.maxAllowed.toFloat(),
                    steps = if (metric.isInteger) (metric.maxAllowed - metric.minAllowed).toInt() - 1 else 0,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentTeal,
                        activeTrackColor = PrimaryBlue,
                        inactiveTrackColor = CardBorder
                    )
                )

                IconButton(
                    onClick = {
                        val next = (value + metric.defaultStep).coerceAtMost(metric.maxAllowed)
                        onValueChange(next)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
