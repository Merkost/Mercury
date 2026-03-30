package com.github.merkost.mercury.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MercurySize.tabHeight)
            .padding(horizontal = MercurySpacing.lg),
        verticalAlignment = Alignment.Bottom
    ) {
        tabs.forEach { tab ->
            MercuryTabItem(
                tab = tab,
                isSelected = tab.id == selectedTabId,
                onClick = { onTabSelected(tab.id) }
            )
        }
    }
}

@Composable
private fun MercuryTabItem(
    tab: MercuryTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 1.dp else 0.dp,
        animationSpec = tween(MercuryMotion.durationNormal)
    )

    Column(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = MercurySpacing.md)
            .padding(bottom = MercurySpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = tab.label,
            style = if (isSelected) typography.titleSmall else typography.bodyMedium,
            color = if (isSelected) colors.textPrimary else colors.textMuted
        )
        Spacer(modifier = Modifier.height(MercurySpacing.xs))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(MercurySize.tabIndicatorHeight)
                    .width(indicatorWidth.coerceAtLeast(1.dp))
                    .fillMaxWidth()
                    .background(colors.accent)
            )
        } else {
            Spacer(modifier = Modifier.height(MercurySize.tabIndicatorHeight))
        }
    }
}
