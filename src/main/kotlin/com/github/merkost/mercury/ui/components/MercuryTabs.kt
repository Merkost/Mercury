package com.github.merkost.mercury.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

data class MercuryTab(
    val id: String,
    val label: String
)

@Composable
fun MercuryTabBar(
    tabs: List<MercuryTab>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val density = LocalDensity.current

    var tabPositions by remember { mutableStateOf(mapOf<String, Pair<Float, Int>>()) }

    val selectedPos = tabPositions[selectedTabId]
    val indicatorOffsetTarget = selectedPos?.let { with(density) { it.first.toDp() } } ?: 0.dp
    val indicatorWidthTarget = selectedPos?.let { with(density) { it.second.toDp() } } ?: 0.dp

    val indicatorOffset by animateDpAsState(
        targetValue = indicatorOffsetTarget,
        animationSpec = tween(MercuryMotion.durationNormal, easing = MercuryMotion.easingDecelerate)
    )
    val indicatorWidth by animateDpAsState(
        targetValue = indicatorWidthTarget,
        animationSpec = tween(MercuryMotion.durationNormal, easing = MercuryMotion.easingDecelerate)
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MercurySize.tabHeight)
                .padding(horizontal = MercurySpacing.lg),
            verticalAlignment = Alignment.Bottom
        ) {
            tabs.forEach { tab ->
                MercuryTabItem(
                    tab = tab,
                    isSelected = tab.id == selectedTabId,
                    onClick = { onTabSelected(tab.id) },
                    onPositioned = { x, size ->
                        tabPositions = tabPositions + (tab.id to Pair(x, size.width))
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset + MercurySpacing.lg)
                .width(indicatorWidth)
                .height(MercurySize.tabIndicatorHeight)
                .align(Alignment.BottomStart)
                .background(colors.accent)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MercurySize.borderWidth)
                .align(Alignment.BottomCenter)
                .background(colors.borderSubtle)
        )
    }
}

@Composable
private fun MercuryTabItem(
    tab: MercuryTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPositioned: (Float, IntSize) -> Unit
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var isHovered by rememberHoverState()

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.textPrimary
            isHovered -> colors.textSecondary
            else -> colors.textMuted
        },
        animationSpec = tween(MercuryMotion.durationFast)
    )

    Box(
        modifier = Modifier
            .onHoverChanged { isHovered = it }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = MercurySpacing.md)
            .padding(bottom = MercurySpacing.sm)
            .onGloballyPositioned { coordinates ->
                onPositioned(coordinates.positionInParent().x, coordinates.size)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tab.label,
            style = if (isSelected) typography.titleSmall else typography.bodyMedium,
            color = textColor
        )
    }
}
