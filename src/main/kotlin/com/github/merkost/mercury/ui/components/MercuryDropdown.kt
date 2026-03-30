package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
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

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .border(MercurySize.borderWidth, colors.border, RoundedCornerShape(MercurySize.radiusMd))
                .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.sm),
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
                color = colors.textMuted
            )
        }

        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false }
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .background(colors.surfaceRaised, RoundedCornerShape(MercurySize.radiusMd))
                        .border(MercurySize.borderWidth, colors.border, RoundedCornerShape(MercurySize.radiusMd))
                ) {
                    options.forEach { option ->
                        Text(
                            text = option,
                            style = typography.bodyMedium,
                            color = if (option == selectedValue) colors.textPrimary else colors.textSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOptionSelected(option)
                                    expanded = false
                                }
                                .padding(horizontal = MercurySpacing.md, vertical = MercurySpacing.sm)
                        )
                    }
                }
            }
        }
    }
}
