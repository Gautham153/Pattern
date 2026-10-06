package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.PatternAnalytics
import com.example.data.model.AnomalyRecord
import com.example.data.model.AnomalySeverity
import com.example.data.model.DailyEntry
import com.example.data.model.MetricStats
import com.example.data.model.MetricType
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AnomalyOrange
import com.example.ui.theme.AnomalyRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ChartAreaShade
import com.example.ui.theme.ChartGridLine
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min

data class ChartDataPoint(
    val entry: DailyEntry,
    val value: Double,
    val formattedDate: String,
    val isAnomaly: Boolean,
    val severity: AnomalySeverity
)

@Composable
fun TrendLineChart(
    metric: MetricType,
    entries: List<DailyEntry>,
    stats: MetricStats?,
    modifier: Modifier = Modifier,
    anomalies: List<AnomalyRecord> = emptyList(),
    onPointClicked: ((DailyEntry) -> Unit)? = null
) {
    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(CardBackground, RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "No data points recorded for this range.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
        return
    }

    // Chronological order for chart from left (oldest) to right (newest)
    val sortedChronological = remember(entries) {
        entries.sortedBy { it.date }
    }

    val points = remember(sortedChronological, anomalies, metric) {
        sortedChronological.map { entry ->
            val value = entry.getMetricValue(metric)
            val anomalyRecord = anomalies.find { it.date == entry.date && it.metric == metric }
            val severity = anomalyRecord?.severity ?: AnomalySeverity.NORMAL

            ChartDataPoint(
                entry = entry,
                value = value,
                formattedDate = entry.formattedDateShort(),
                isAnomaly = severity != AnomalySeverity.NORMAL,
                severity = severity
            )
        }
    }

    var selectedPointIndex by remember(entries, metric) {
        // default to latest anomaly if any, or latest point
        val lastAnomalyIdx = points.indexOfLast { it.isAnomaly }
        mutableStateOf(if (lastAnomalyIdx >= 0) lastAnomalyIdx else points.lastIndex)
    }

    val selectedPoint = points.getOrNull(selectedPointIndex)

    // Calculate dynamic Y range
    val rawMin = points.minOfOrNull { it.value } ?: 0.0
    val rawMax = points.maxOfOrNull { it.value } ?: metric.maxAllowed
    val yMin = 0.0
    val yMax = when {
        metric == MetricType.MOOD || metric == MetricType.PRODUCTIVITY -> 10.0
        rawMax <= 12.0 && metric.maxAllowed <= 16.0 -> max(12.0, (rawMax * 1.15))
        metric == MetricType.EXERCISE -> max(60.0, ((rawMax / 30.0).toInt() + 1) * 30.0)
        else -> max(metric.maxAllowed, rawMax * 1.1)
    }

    Box(
        modifier = modifier
            .testTag("trend_line_chart")
            .fillMaxWidth()
            .height(280.dp)
            .background(CardBackground, RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chart Title & Subtitle / Selected point summary
            Text(
                text = "${metric.title} (${metric.unit})",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { tapOffset ->
                                val width = size.width
                                val paddingLeft = 40f
                                val paddingRight = 20f
                                val chartWidth = width - paddingLeft - paddingRight

                                if (points.isNotEmpty()) {
                                    val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth
                                    val tappedIndex = ((tapOffset.x - paddingLeft + stepX / 2) / stepX)
                                        .toInt()
                                        .coerceIn(0, points.lastIndex)
                                    selectedPointIndex = tappedIndex
                                    onPointClicked?.invoke(points[tappedIndex].entry)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val padLeft = 40f
                    val padRight = 20f
                    val padTop = 20f
                    val padBottom = 30f

                    val chartWidth = width - padLeft - padRight
                    val chartHeight = height - padTop - padBottom

                    // Draw 4 Horizontal Grid lines and Y labels
                    val ySteps = 4
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(140, 148, 163, 184)
                        textSize = 26f
                        isAntiAlias = true
                    }

                    for (i in 0..ySteps) {
                        val fraction = i.toFloat() / ySteps
                        val yPos = padTop + chartHeight * (1f - fraction)
                        val stepValue = yMin + fraction * (yMax - yMin)

                        // Grid line
                        drawLine(
                            color = ChartGridLine,
                            start = Offset(padLeft, yPos),
                            end = Offset(width - padRight, yPos),
                            strokeWidth = 1.5f
                        )

                        // Y Label
                        val label = if (metric.isInteger || stepValue >= 10) "${stepValue.toInt()}" else String.format("%.1f", stepValue)
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            10f,
                            yPos + 8f,
                            textPaint
                        )
                    }

                    // Shaded Normal Range Band if available
                    if (stats != null && stats.count >= 3) {
                        val bandTopVal = min(yMax, stats.normalHigh)
                        val bandBottomVal = max(yMin, stats.normalLow)

                        val bandTopY = padTop + chartHeight * (1f - ((bandTopVal - yMin) / (yMax - yMin)).toFloat()).coerceIn(0f, 1f)
                        val bandBottomY = padTop + chartHeight * (1f - ((bandBottomVal - yMin) / (yMax - yMin)).toFloat()).coerceIn(0f, 1f)

                        drawRect(
                            color = Color(0x0C38BDF8),
                            topLeft = Offset(padLeft, bandTopY),
                            size = Size(chartWidth, max(2f, bandBottomY - bandTopY))
                        )
                    }

                    if (points.isEmpty()) return@Canvas

                    val stepX = if (points.size > 1) chartWidth / (points.size - 1) else 0f
                    val pointOffsets = mutableListOf<Offset>()

                    for (i in points.indices) {
                        val p = points[i]
                        val x = padLeft + i * stepX
                        val normY = ((p.value - yMin) / (yMax - yMin)).toFloat().coerceIn(0f, 1f)
                        val y = padTop + chartHeight * (1f - normY)
                        pointOffsets.add(Offset(x, y))
                    }

                    // Draw Gradient Area fill under trend line
                    val areaPath = Path().apply {
                        moveTo(pointOffsets.first().x, padTop + chartHeight)
                        for (pt in pointOffsets) {
                            lineTo(pt.x, pt.y)
                        }
                        lineTo(pointOffsets.last().x, padTop + chartHeight)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AccentTeal.copy(alpha = 0.25f),
                                AccentTeal.copy(alpha = 0.02f)
                            ),
                            startY = padTop,
                            endY = padTop + chartHeight
                        )
                    )

                    // Draw Trend Line
                    val linePath = Path().apply {
                        moveTo(pointOffsets.first().x, pointOffsets.first().y)
                        for (i in 1 until pointOffsets.size) {
                            lineTo(pointOffsets[i].x, pointOffsets[i].y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = AccentTeal,
                        style = Stroke(
                            width = 3.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw X Date Labels
                    val labelStep = max(1, points.size / 6)
                    for (i in points.indices) {
                        if (i % labelStep == 0 || i == points.lastIndex) {
                            val pt = pointOffsets[i]
                            val dateText = points[i].formattedDate
                            drawContext.canvas.nativeCanvas.drawText(
                                dateText,
                                pt.x - 24f,
                                padTop + chartHeight + 28f,
                                textPaint
                            )
                        }
                    }

                    // Draw Data Points and Anomaly Rings
                    for (i in points.indices) {
                        val p = points[i]
                        val pt = pointOffsets[i]
                        val isSelected = (i == selectedPointIndex)

                        when {
                            p.severity == AnomalySeverity.VERY_UNUSUAL -> {
                                drawCircle(
                                    color = AnomalyRed.copy(alpha = 0.35f),
                                    radius = if (isSelected) 10f else 8f,
                                    center = pt
                                )
                                drawCircle(
                                    color = AnomalyRed,
                                    radius = if (isSelected) 6f else 5f,
                                    center = pt
                                )
                            }
                            p.severity == AnomalySeverity.UNUSUAL -> {
                                drawCircle(
                                    color = AnomalyOrange.copy(alpha = 0.35f),
                                    radius = if (isSelected) 9f else 7f,
                                    center = pt
                                )
                                drawCircle(
                                    color = AnomalyOrange,
                                    radius = if (isSelected) 5.5f else 4.5f,
                                    center = pt
                                )
                            }
                            else -> {
                                drawCircle(
                                    color = if (isSelected) AccentTeal else PrimaryBlue,
                                    radius = if (isSelected) 5f else 3.5f,
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = if (isSelected) 2.5f else 1.5f,
                                    center = pt
                                )
                            }
                        }

                        // If selected, draw a vertical indicator guideline and callout marker
                        if (isSelected) {
                            drawLine(
                                color = AccentTeal.copy(alpha = 0.6f),
                                start = Offset(pt.x, padTop),
                                end = Offset(pt.x, padTop + chartHeight),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )

                            // Tooltip Callout
                            val tooltipText = "${p.formattedDate}: ${metric.formatValue(p.value)}"
                            val tipPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 26f
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                            val textWidth = tipPaint.measureText(tooltipText)
                            val bubbleX = (pt.x - textWidth / 2).coerceIn(padLeft, width - padRight - textWidth - 16f)
                            val bubbleY = (pt.y - 20f).coerceAtLeast(padTop + 20f)

                            val bubbleBgPaint = android.graphics.Paint().apply {
                                color = if (p.isAnomaly) android.graphics.Color.argb(220, 239, 68, 68) else android.graphics.Color.argb(220, 24, 34, 51)
                                style = android.graphics.Paint.Style.FILL
                                isAntiAlias = true
                            }

                            drawContext.canvas.nativeCanvas.drawRoundRect(
                                bubbleX - 10f,
                                bubbleY - 26f,
                                bubbleX + textWidth + 10f,
                                bubbleY + 10f,
                                12f,
                                12f,
                                bubbleBgPaint
                            )

                            drawContext.canvas.nativeCanvas.drawText(
                                tooltipText,
                                bubbleX,
                                bubbleY,
                                tipPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
