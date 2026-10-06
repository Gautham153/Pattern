package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector

enum class MetricType(
    val id: String,
    val title: String,
    val shortTitle: String,
    val unit: String,
    val unitSuffix: String,
    val minAllowed: Double,
    val maxAllowed: Double,
    val isInteger: Boolean,
    val defaultStep: Double
) {
    SLEEP(
        id = "sleep",
        title = "Sleep",
        shortTitle = "Sleep",
        unit = "hours",
        unitSuffix = "h",
        minAllowed = 0.0,
        maxAllowed = 12.0,
        isInteger = false,
        defaultStep = 0.5
    ),
    SCREEN_TIME(
        id = "screen_time",
        title = "Screen Time",
        shortTitle = "Screen",
        unit = "hours",
        unitSuffix = "h",
        minAllowed = 0.0,
        maxAllowed = 16.0,
        isInteger = false,
        defaultStep = 0.5
    ),
    STUDY(
        id = "study",
        title = "Study",
        shortTitle = "Study",
        unit = "hours",
        unitSuffix = "h",
        minAllowed = 0.0,
        maxAllowed = 12.0,
        isInteger = false,
        defaultStep = 0.5
    ),
    EXERCISE(
        id = "exercise",
        title = "Exercise",
        shortTitle = "Exercise",
        unit = "minutes",
        unitSuffix = "m",
        minAllowed = 0.0,
        maxAllowed = 240.0,
        isInteger = true,
        defaultStep = 5.0
    ),
    MOOD(
        id = "mood",
        title = "Mood",
        shortTitle = "Mood",
        unit = "score",
        unitSuffix = "/10",
        minAllowed = 1.0,
        maxAllowed = 10.0,
        isInteger = true,
        defaultStep = 1.0
    ),
    PRODUCTIVITY(
        id = "productivity",
        title = "Productivity",
        shortTitle = "Prod.",
        unit = "score",
        unitSuffix = "/10",
        minAllowed = 1.0,
        maxAllowed = 10.0,
        isInteger = true,
        defaultStep = 1.0
    );

    val icon: ImageVector
        get() = when (this) {
            SLEEP -> Icons.Filled.Bedtime
            SCREEN_TIME -> Icons.Filled.Smartphone
            STUDY -> Icons.Filled.MenuBook
            EXERCISE -> Icons.Filled.DirectionsRun
            MOOD -> Icons.Filled.Mood
            PRODUCTIVITY -> Icons.Filled.Bolt
        }

    fun formatValue(value: Double): String {
        return when {
            this == MOOD || this == PRODUCTIVITY -> "${value.toInt()}/10"
            this == EXERCISE -> "${value.toInt()} min"
            else -> {
                val formatted = String.format("%.1f", value)
                "${formatted} h"
            }
        }
    }

    fun formatCompact(value: Double): String {
        return when {
            this == MOOD || this == PRODUCTIVITY -> "${value.toInt()}"
            this == EXERCISE -> "${value.toInt()}m"
            else -> {
                val formatted = String.format("%.1f", value)
                "${formatted}h"
            }
        }
    }
}
