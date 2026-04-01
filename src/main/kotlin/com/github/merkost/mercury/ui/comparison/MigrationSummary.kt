package com.github.merkost.mercury.ui.comparison

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.github.merkost.mercury.model.SchemaDiff
import com.github.merkost.mercury.model.MatchType
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MigrationSummary(
    leftLabel: String,
    rightLabel: String,
    diff: SchemaDiff,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    val added = diff.rightOnly.size
    val removed = diff.leftOnly.size
    val modified = diff.matchedEntities.count { it.matchType != MatchType.IDENTICAL }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MercurySpacing.lg, vertical = MercurySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$leftLabel \u2192 $rightLabel: ",
            style = typography.labelMedium,
            color = colors.textSecondary
        )
        Text(
            text = "+$added",
            style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = colors.diffAdd
        )
        Text(text = " added \u00B7 ", style = typography.labelSmall, color = colors.textMuted)
        Text(
            text = "-$removed",
            style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = colors.diffRemove
        )
        Text(text = " removed \u00B7 ", style = typography.labelSmall, color = colors.textMuted)
        Text(
            text = "~$modified",
            style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = colors.diffSimilar
        )
        Text(text = " modified", style = typography.labelSmall, color = colors.textMuted)
    }
}
