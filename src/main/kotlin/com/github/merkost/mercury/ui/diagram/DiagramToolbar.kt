package com.github.merkost.mercury.ui.diagram

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.MercuryBundle
import com.github.merkost.mercury.ui.components.onHoverChanged
import com.github.merkost.mercury.ui.components.rememberHoverState
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun DiagramToolbar(
    zoom: Float,
    entityCount: Int,
    relationCount: Int,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onResetView: () -> Unit,
    onFitAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MercurySize.tabHeight)
            .background(colors.surface)
            .padding(horizontal = MercurySpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarButton(text = "\u2212", onClick = onZoomOut)
        Spacer(Modifier.width(MercurySpacing.xs))
        ToolbarButton(
            text = "${(zoom * 100).toInt()}%",
            onClick = onResetZoom
        )
        Spacer(Modifier.width(MercurySpacing.xs))
        ToolbarButton(text = "+", onClick = onZoomIn)

        ToolbarSeparator()

        ToolbarButton(text = MercuryBundle.message("diagram.toolbar.resetView"), onClick = onResetView)
        Spacer(Modifier.width(MercurySpacing.xs))
        ToolbarButton(text = MercuryBundle.message("diagram.toolbar.fitAll"), onClick = onFitAll)

        ToolbarSeparator()

        Spacer(Modifier.weight(1f))

        Text(
            text = "$entityCount entities \u00B7 $relationCount relations",
            style = typography.labelSmall,
            color = colors.textMuted
        )
    }
}

@Composable
private fun ToolbarSeparator() {
    val colors = MercuryTheme.colors
    Spacer(Modifier.width(MercurySpacing.sm))
    Box(
        modifier = Modifier
            .width(MercurySize.borderWidth)
            .height(MercurySpacing.lg)
            .background(colors.borderSubtle)
    )
    Spacer(Modifier.width(MercurySpacing.sm))
}

@Composable
private fun ToolbarButton(text: String, onClick: () -> Unit) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var isHovered by rememberHoverState()

    val textColor by animateColorAsState(
        targetValue = if (isHovered) colors.textSecondary else colors.textMuted,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    Text(
        text = text,
        style = typography.labelMedium,
        color = textColor,
        modifier = Modifier
            .onHoverChanged { isHovered = it }
            .clickable(onClick = onClick)
            .padding(horizontal = MercurySpacing.sm, vertical = MercurySpacing.xs)
    )
}
