package com.github.merkost.mercury.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.MercuryBundle
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryErrorBanner(
    message: String,
    visible: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.xs)
                .background(colors.errorBanner, RoundedCornerShape(MercurySize.radiusMd))
                .border(MercurySize.borderWidth, colors.errorBannerBorder, RoundedCornerShape(MercurySize.radiusMd))
                .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = typography.labelMedium,
                    color = colors.error
                )
                Text(
                    text = MercuryBundle.message("state.error.hint"),
                    style = typography.labelSmall,
                    color = colors.textMuted
                )
            }
            Text(
                text = MercuryBundle.message("state.error.retry"),
                style = typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier
                    .border(MercurySize.borderWidth, colors.errorBannerBorder, RoundedCornerShape(MercurySize.radiusSm))
                    .clickable(onClick = onRetry)
                    .padding(horizontal = MercurySpacing.sm, vertical = MercurySpacing.xs)
            )
        }
    }
}
