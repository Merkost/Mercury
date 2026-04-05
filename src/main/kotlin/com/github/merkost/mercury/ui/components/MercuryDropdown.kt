package com.github.merkost.mercury.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.Popup
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun MercuryDropdown(
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var expanded by remember { mutableStateOf(false) }
    var isHovered by rememberHoverState()
    var focusedIndex by remember { mutableStateOf(-1) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(expanded) {
        if (expanded) focusRequester.requestFocus()
    }

    val triggerBackground by animateColorAsState(
        targetValue = if (isHovered || expanded) colors.surfaceHover else Color.Transparent,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(MercuryMotion.durationNormal)
    )

    val menuAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .onHoverChanged { isHovered = it }
                .clickable {
                    expanded = !expanded
                    if (expanded) focusedIndex = options.indexOf(selectedValue)
                }
                .background(triggerBackground, RoundedCornerShape(MercurySize.radiusMd))
                .border(MercurySize.borderWidth, colors.border, RoundedCornerShape(MercurySize.radiusMd))
                .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.sm)
                .focusRequester(focusRequester)
                .onKeyEvent { event ->
                    if (!expanded) return@onKeyEvent false
                    when {
                        event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown -> {
                            focusedIndex = (focusedIndex + 1).coerceAtMost(options.size - 1)
                            true
                        }
                        event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp -> {
                            focusedIndex = (focusedIndex - 1).coerceAtLeast(0)
                            true
                        }
                        event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                            if (focusedIndex in options.indices) {
                                onOptionSelected(options[focusedIndex])
                                expanded = false
                            }
                            true
                        }
                        event.type == KeyEventType.KeyDown && event.key == Key.Escape -> {
                            expanded = false
                            true
                        }
                        else -> false
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedValue,
                style = typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.width(MercurySpacing.xs))
            Text(
                text = "\u25BE",
                style = typography.bodySmall,
                color = colors.textMuted,
                modifier = Modifier.rotate(chevronRotation)
            )
        }

        if (expanded) {
            Popup(onDismissRequest = { expanded = false }) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .alpha(menuAlpha)
                        .background(colors.surfaceRaised, RoundedCornerShape(MercurySize.radiusMd))
                        .border(MercurySize.borderWidth, colors.border, RoundedCornerShape(MercurySize.radiusMd))
                ) {
                    options.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = option,
                            isSelected = option == selectedValue,
                            isFocused = index == focusedIndex,
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownMenuItem(
    text: String,
    isSelected: Boolean,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var isHovered by rememberHoverState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused || isHovered -> colors.surfaceHover
            else -> Color.Transparent
        },
        animationSpec = tween(MercuryMotion.durationFast)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .onHoverChanged { isHovered = it }
            .clickable(onClick = onClick)
            .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isSelected) "\u2713" else " ",
            style = typography.labelSmall,
            color = colors.accent,
            modifier = Modifier.width(MercurySpacing.lg)
        )
        Text(
            text = text,
            style = typography.bodyMedium,
            color = if (isSelected) colors.textPrimary else colors.textSecondary
        )
    }
}
