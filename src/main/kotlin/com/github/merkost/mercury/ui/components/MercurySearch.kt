package com.github.merkost.mercury.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercurySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search entities, fields, queries...",
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) colors.borderFocus else colors.border,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    val borderWidth = if (isFocused) MercurySize.borderWidthFocus else MercurySize.borderWidth

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = typography.bodyMedium.copy(color = colors.textPrimary),
        cursorBrush = SolidColor(colors.accent),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(MercurySize.searchHeight)
            .border(borderWidth, borderColor, RoundedCornerShape(MercurySize.radiusMd))
            .padding(horizontal = MercurySpacing.md)
            .onFocusChanged { isFocused = it.isFocused },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    val showPlaceholder = query.isEmpty() && !isFocused
                    Text(
                        text = placeholder,
                        style = typography.bodyMedium,
                        color = colors.textMuted,
                        modifier = Modifier.alpha(if (showPlaceholder) 1f else 0f)
                    )
                    innerTextField()
                }
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(tween(MercuryMotion.durationFast)),
                    exit = fadeOut(tween(MercuryMotion.durationFast))
                ) {
                    Text(
                        text = "\u2715",
                        style = typography.labelMedium,
                        color = colors.textMuted,
                        modifier = Modifier
                            .clickable { onQueryChange("") }
                            .padding(start = MercurySpacing.xs)
                    )
                }
            }
        }
    )
}
