package com.github.merkost.mercury.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
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

    val borderColor = if (isFocused) colors.borderFocus else colors.border
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight()
            ) {
                innerTextField()
            }
        }
    )
}
