package com.github.merkost.mercury.ui.comparison

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.model.ColumnDiff
import com.github.merkost.mercury.model.ColumnDiffType
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ColumnDiffBlock(columnDiffs: List<ColumnDiff>, isExpanded: Boolean, modifier: Modifier = Modifier) {
    val colors = MercuryTheme.colors
    AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically(), modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = MercurySpacing.xs)) {
            for (diff in columnDiffs) { ColumnDiffRow(diff) }
        }
    }
}

@Composable
private fun ColumnDiffRow(diff: ColumnDiff) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    val tint = when (diff.diffType) {
        ColumnDiffType.TYPE_CHANGED, ColumnDiffType.NULLABILITY_CHANGED -> colors.diffSimilar
        ColumnDiffType.LEFT_ONLY -> colors.diffRemove.copy(alpha = 0.3f)
        ColumnDiffType.RIGHT_ONLY -> colors.diffAdd.copy(alpha = 0.3f)
    }
    Row(modifier = Modifier.fillMaxWidth().height(MercurySize.entityFieldHeight).background(tint)) {
        Row(modifier = Modifier.weight(1f).padding(horizontal = MercurySpacing.md), verticalAlignment = Alignment.CenterVertically) {
            if (diff.diffType != ColumnDiffType.RIGHT_ONLY) {
                Text(text = diff.columnName, style = typography.bodySmall, color = colors.textPrimary)
                Spacer(Modifier.weight(1f))
                Text(text = diff.leftType ?: "", style = typography.codeMedium, color = colors.textMuted)
            }
        }
        Box(modifier = Modifier.width(MercurySize.borderWidth).fillMaxHeight().background(colors.borderSubtle))
        Row(modifier = Modifier.weight(1f).padding(horizontal = MercurySpacing.md), verticalAlignment = Alignment.CenterVertically) {
            if (diff.diffType != ColumnDiffType.LEFT_ONLY) {
                Text(text = diff.columnName, style = typography.bodySmall, color = colors.textPrimary)
                Spacer(Modifier.weight(1f))
                Text(text = diff.rightType ?: "", style = typography.codeMedium, color = colors.textMuted)
            }
        }
    }
}
