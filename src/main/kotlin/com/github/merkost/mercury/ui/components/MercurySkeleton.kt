package com.github.merkost.mercury.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.github.merkost.mercury.MercuryBundle
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercurySkeleton(modifier: Modifier = Modifier) {
    val colors = MercuryTheme.colors
    val transition = rememberInfiniteTransition()
    val translateX by transition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(colors.surfaceRaised, colors.surfaceHover, colors.surfaceRaised),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 200f, 0f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MercurySpacing.lg, vertical = MercurySpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonBar(shimmerBrush, fraction = 0.5f, height = MercurySize.sectionHeaderHeight)
        Spacer(Modifier.height(MercurySpacing.sm))
        SkeletonBar(shimmerBrush, fraction = 0.6f, indent = MercurySpacing.lg)
        Spacer(Modifier.height(MercurySpacing.xs))
        SkeletonBar(shimmerBrush, fraction = 0.45f, indent = MercurySpacing.lg)
        Spacer(Modifier.height(MercurySpacing.xs))
        SkeletonBar(shimmerBrush, fraction = 0.55f, indent = MercurySpacing.lg)
        Spacer(Modifier.height(MercurySpacing.md))
        SkeletonBar(shimmerBrush, fraction = 0.35f, height = MercurySize.sectionHeaderHeight)
        Spacer(Modifier.height(MercurySpacing.sm))
        SkeletonBar(shimmerBrush, fraction = 0.5f, indent = MercurySpacing.lg)
        Spacer(Modifier.height(MercurySpacing.lg))
        Text(
            text = MercuryBundle.message("state.loading"),
            style = MercuryTheme.typography.labelMedium,
            color = MercuryTheme.colors.textMuted
        )
    }
}

@Composable
private fun SkeletonBar(
    brush: Brush,
    fraction: Float,
    indent: androidx.compose.ui.unit.Dp = MercurySpacing.xxs,
    height: androidx.compose.ui.unit.Dp = MercurySize.treeRowHeight
) {
    Box(
        modifier = Modifier
            .padding(start = indent)
            .fillMaxWidth(fraction)
            .height(height)
            .background(brush, RoundedCornerShape(MercurySize.radiusSm))
    )
}
