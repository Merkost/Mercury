package com.github.merkost.mercury.ui.comparison

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.merkost.mercury.model.EntityInfo
import com.github.merkost.mercury.model.MatchType
import com.github.merkost.mercury.ui.components.onHoverChanged
import com.github.merkost.mercury.ui.components.rememberHoverState
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun EntityDiffRow(
    leftEntity: EntityInfo?,
    rightEntity: EntityInfo?,
    matchType: MatchType?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    var isHovered by rememberHoverState()
    val tintColor = when (matchType) {
        MatchType.IDENTICAL -> colors.diffAdd
        MatchType.SIMILAR, MatchType.NAME_ONLY -> colors.diffSimilar
        null -> Color.Transparent
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) colors.surfaceHover else tintColor,
        animationSpec = tween(MercuryMotion.durationFast)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MercurySize.treeRowHeight)
            .background(backgroundColor)
            .onHoverChanged { isHovered = it }
            .clickable(onClick = onClick)
    ) {
        EntityCell(entity = leftEntity, matchSymbol = matchSymbol(matchType, true), modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(MercurySize.borderWidth).fillMaxHeight().background(colors.borderSubtle))
        EntityCell(entity = rightEntity, matchSymbol = matchSymbol(matchType, false), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EntityCell(entity: EntityInfo?, matchSymbol: String, modifier: Modifier = Modifier) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    if (entity == null) { Spacer(modifier = modifier); return }
    Row(modifier = modifier.padding(horizontal = MercurySpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Text(text = matchSymbol, style = typography.labelSmall, color = colors.textMuted, modifier = Modifier.width(MercurySpacing.lg))
        Text(text = entity.name, style = typography.titleSmall, color = colors.textPrimary)
        Spacer(Modifier.weight(1f))
        Text(text = entity.tableName, style = typography.codeMedium, color = colors.textMuted)
        Spacer(Modifier.width(MercurySpacing.xs))
    }
}

private fun matchSymbol(matchType: MatchType?, isLeft: Boolean): String = when {
    matchType == MatchType.IDENTICAL -> "="
    matchType == MatchType.SIMILAR || matchType == MatchType.NAME_ONLY -> "\u2248"
    matchType == null -> "\u00B7"
    else -> ""
}
