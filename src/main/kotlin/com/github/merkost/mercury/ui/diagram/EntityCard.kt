package com.github.merkost.mercury.ui.diagram

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import com.github.merkost.mercury.ui.components.onHoverChanged
import com.github.merkost.mercury.ui.components.rememberHoverState
import com.github.merkost.mercury.model.ColumnInfo
import com.github.merkost.mercury.model.EntityInfo
import com.github.merkost.mercury.ui.theme.MercuryElevation
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun EntityCard(
    entity: EntityInfo,
    isSelected: Boolean,
    isDimmed: Boolean,
    zoom: Float,
    onSelect: () -> Unit,
    onDoubleClick: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    var isHovered by rememberHoverState()
    var isDragging by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.4f else 1f,
        animationSpec = tween(MercuryMotion.durationNormal)
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.accent
            isHovered -> colors.borderFocus
            else -> colors.border
        },
        animationSpec = tween(MercuryMotion.durationFast)
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) MercurySize.borderWidthFocus else MercurySize.borderWidth,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isDragging -> MercuryElevation.overlay
            isHovered -> MercuryElevation.raised
            else -> MercuryElevation.subtle
        },
        animationSpec = tween(MercuryMotion.durationFast)
    )

    Column(
        modifier = modifier
            .widthIn(min = MercurySize.entityCardMinWidth, max = MercurySize.entityCardMaxWidth)
            .alpha(alpha)
            .shadow(elevation, RoundedCornerShape(MercurySize.radiusLg))
            .background(colors.surfaceRaised, RoundedCornerShape(MercurySize.radiusLg))
            .border(borderWidth, borderColor, RoundedCornerShape(MercurySize.radiusLg))
            .onHoverChanged { isHovered = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onDoubleTap = { onDoubleClick() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onDragStart()
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        onDragEnd()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
    ) {
        CardHeader(entity)
        CardFields(entity, zoom)
    }
}

@Composable
private fun CardHeader(entity: EntityInfo) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceRaised)
            .padding(horizontal = MercurySpacing.sm, vertical = MercurySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entity.name,
            style = typography.titleSmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(MercurySpacing.xs))
        Text(
            text = entity.tableName,
            style = typography.codeSmall,
            color = colors.textMuted,
            maxLines = 1
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MercurySize.borderWidth)
            .background(colors.borderSubtle)
    )
}

@Composable
private fun CardFields(entity: EntityInfo, zoom: Float) {
    val pkColumns = entity.primaryKey.columnNames.toSet()
    val fkColumns = entity.foreignKeys.flatMap { it.childColumns }.toSet()

    if (zoom < 0.3f) return

    Column(
        modifier = Modifier.padding(horizontal = MercurySpacing.sm, vertical = MercurySpacing.xs)
    ) {
        for (column in entity.columns) {
            val isPk = column.columnName in pkColumns
            val isFk = column.columnName in fkColumns
            val hasConverter = column.typeConverter != null
            CardFieldRow(column, isPk, isFk, hasConverter, showType = zoom >= 0.5f)
        }
    }
}

@Composable
private fun CardFieldRow(
    column: ColumnInfo,
    isPk: Boolean,
    isFk: Boolean,
    hasConverter: Boolean,
    showType: Boolean
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    val symbol = when {
        isPk -> "\u25CF "
        isFk -> "\u25CB "
        hasConverter -> "~ "
        else -> "  "
    }

    val nameColor = when {
        isPk -> colors.textPrimary
        isFk -> colors.textSecondary
        else -> colors.textMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MercurySize.entityFieldHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$symbol${column.name}",
            style = typography.bodySmall,
            color = nameColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (showType) {
            Spacer(Modifier.width(MercurySpacing.xs))
            Text(
                text = column.type,
                style = typography.codeSmall,
                color = colors.textMuted,
                maxLines = 1
            )
        }
    }
}
