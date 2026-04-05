package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.merkost.mercury.MercuryBundle
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryEmptyState(modifier: Modifier = Modifier) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\u2205",
                style = typography.titleLarge.copy(fontSize = 22.sp),
                color = colors.textMuted
            )
            Spacer(Modifier.height(MercurySpacing.sm))
            Text(
                text = MercuryBundle.message("state.empty.title"),
                style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textSecondary
            )
            Spacer(Modifier.height(MercurySpacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Add ",
                    style = typography.labelMedium,
                    color = colors.textMuted
                )
                Text(
                    text = "@Database",
                    style = typography.codeSmall,
                    color = colors.textMuted,
                    modifier = Modifier
                        .background(colors.surfaceRaised, RoundedCornerShape(MercurySize.radiusSm))
                        .padding(horizontal = MercurySpacing.xs, vertical = MercurySpacing.xxs)
                )
                Text(
                    text = " to a class or sync Gradle",
                    style = typography.labelMedium,
                    color = colors.textMuted
                )
            }
        }
    }
}
