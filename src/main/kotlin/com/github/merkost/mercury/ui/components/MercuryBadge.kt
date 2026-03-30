package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Text(
        text = text,
        style = typography.labelSmall,
        color = colors.textMuted,
        modifier = modifier
            .background(
                color = colors.surfaceRaised,
                shape = RoundedCornerShape(MercurySize.radiusSm)
            )
            .padding(horizontal = MercurySpacing.xs, vertical = MercurySpacing.xxs)
    )
}

@Composable
fun MercuryCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    MercuryBadge(
        text = count.toString(),
        modifier = modifier
    )
}
