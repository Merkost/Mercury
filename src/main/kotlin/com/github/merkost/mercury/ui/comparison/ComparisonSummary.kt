package com.github.merkost.mercury.ui.comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.model.MatchType
import com.github.merkost.mercury.model.SchemaDiff
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ComparisonSummary(diff: SchemaDiff, modifier: Modifier = Modifier) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    val identical = diff.matchedEntities.count { it.matchType == MatchType.IDENTICAL }
    val differs = diff.matchedEntities.count { it.matchType != MatchType.IDENTICAL }
    Row(
        modifier = modifier.fillMaxWidth().height(MercurySize.tabHeight).background(colors.surface).padding(horizontal = MercurySpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$identical identical \u00B7 $differs differs \u00B7 ${diff.leftOnly.size} left only \u00B7 ${diff.rightOnly.size} right only",
            style = typography.labelSmall,
            color = colors.textMuted
        )
    }
}
