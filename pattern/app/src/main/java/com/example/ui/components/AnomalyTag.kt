package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnomalySeverity
import com.example.ui.theme.AnomalyAmber
import com.example.ui.theme.AnomalyAmberBg
import com.example.ui.theme.AnomalyOrange
import com.example.ui.theme.AnomalyOrangeBg
import com.example.ui.theme.AnomalyRed
import com.example.ui.theme.AnomalyRedBg
import com.example.ui.theme.NormalGreen
import com.example.ui.theme.NormalGreenBg

@Composable
fun AnomalyTag(
    severity: AnomalySeverity,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val (textColor, bgColor, borderColor) = when (severity) {
        AnomalySeverity.VERY_UNUSUAL -> Triple(AnomalyRed, AnomalyRedBg, AnomalyRed.copy(alpha = 0.4f))
        AnomalySeverity.UNUSUAL -> Triple(AnomalyOrange, AnomalyOrangeBg, AnomalyOrange.copy(alpha = 0.4f))
        AnomalySeverity.NORMAL -> Triple(NormalGreen, NormalGreenBg, NormalGreen.copy(alpha = 0.4f))
    }

    val label = customText ?: severity.label

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
