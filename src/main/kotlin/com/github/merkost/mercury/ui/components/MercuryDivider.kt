package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryDottedDivider(
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MercurySpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DottedLine(modifier = Modifier.weight(1f))
        if (label != null) {
            Text(
                text = label,
                style = typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.padding(horizontal = MercurySpacing.sm)
            )
            DottedLine(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DottedLine(modifier: Modifier = Modifier) {
    val color = MercuryTheme.colors.borderSubtle
    Canvas(
        modifier = modifier.height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    }
}

@Composable
fun MercurySolidDivider(modifier: Modifier = Modifier) {
    val colors = MercuryTheme.colors
    Canvas(
        modifier = modifier.fillMaxWidth().height(1.dp)
    ) {
        drawLine(
            color = colors.borderSubtle,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f)
        )
    }
}
