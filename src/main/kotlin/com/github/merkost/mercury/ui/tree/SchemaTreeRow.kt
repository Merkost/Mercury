package com.github.merkost.mercury.ui.tree

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.merkost.mercury.ui.components.onHoverChanged
import com.github.merkost.mercury.ui.components.rememberHoverState
import androidx.compose.ui.unit.sp
import com.github.merkost.mercury.model.*
import com.github.merkost.mercury.ui.components.MercuryBadge
import com.github.merkost.mercury.ui.components.MercuryCountBadge
import com.github.merkost.mercury.ui.components.MercuryDottedDivider
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SchemaTreeRow(
    node: SchemaTreeNode,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (node is SchemaTreeNode.SectionDivider) {
        DividerRow(node, modifier)
        return
    }

    val colors = MercuryTheme.colors
    var isHovered by rememberHoverState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.surfacePressed
            isHovered -> colors.surfaceHover
            else -> Color.Transparent
        },
        animationSpec = tween(MercuryMotion.durationFast)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (node is SchemaTreeNode.SectionHeader) MercurySize.sectionHeaderHeight else MercurySize.treeRowHeight)
            .background(backgroundColor)
            .drawBehind {
                val guideColor = colors.borderSubtle
                for (level in 1 until node.depth) {
                    val x = (level * MercurySize.treeIndent.toPx()) + (MercurySize.treeIndent.toPx() / 2)
                    drawLine(
                        color = guideColor,
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }
            }
            .onHoverChanged { isHovered = it }
            .clickable(onClick = onClick)
            .padding(horizontal = MercurySpacing.sm)
            .padding(start = (node.depth * MercurySize.treeIndent.value).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(MercurySize.borderWidthFocus)
                .fillMaxHeight()
                .background(if (isSelected) colors.accent else Color.Transparent)
        )
        Spacer(modifier = Modifier.width(MercurySpacing.xs))

        when (node) {
            is SchemaTreeNode.Database -> DatabaseRow(node, isExpanded)
            is SchemaTreeNode.SectionHeader -> SectionHeaderRow(node, isExpanded)
            is SchemaTreeNode.Entity -> EntityRow(node, isExpanded)
            is SchemaTreeNode.Column -> ColumnRow(node)
            is SchemaTreeNode.Index -> IndexRow(node)
            is SchemaTreeNode.ForeignKey -> ForeignKeyRow(node)
            is SchemaTreeNode.View -> ViewRow(node, isExpanded)
            is SchemaTreeNode.TypeConverter -> TypeConverterRow(node)
            is SchemaTreeNode.Dao -> DaoRow(node, isExpanded)
            is SchemaTreeNode.DaoMethodNode -> DaoMethodRow(node)
            is SchemaTreeNode.SectionDivider -> {}
        }
    }
}

@Composable
private fun ExpandIndicator(isExpanded: Boolean) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(150, easing = MercuryMotion.easingStandard)
    )
    Box(
        modifier = Modifier.size(MercurySpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u25B6",
            style = typography.labelSmall,
            color = colors.textMuted,
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
private fun DatabaseRow(node: SchemaTreeNode.Database, isExpanded: Boolean) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    ExpandIndicator(isExpanded)
    Text(text = node.info.name, style = typography.titleSmall, color = colors.textPrimary)
    Spacer(modifier = Modifier.width(MercurySpacing.sm))
    Text(text = "(v${node.info.version})", style = typography.labelSmall, color = colors.textMuted)
}

@Composable
private fun RowScope.SectionHeaderRow(node: SchemaTreeNode.SectionHeader, isExpanded: Boolean) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    ExpandIndicator(isExpanded)
    Text(
        text = when (node.sectionType) {
            SectionType.ENTITIES -> "ENTITIES"
            SectionType.VIEWS -> "VIEWS"
            SectionType.TYPE_CONVERTERS -> "TYPE CONVERTERS"
            SectionType.DAOS -> "DAOS"
        },
        style = typography.labelSmall.copy(letterSpacing = 0.5.sp),
        color = colors.textMuted
    )
    Spacer(modifier = Modifier.weight(1f))
    MercuryCountBadge(node.count)
    Spacer(modifier = Modifier.width(MercurySpacing.sm))
}

@Composable
private fun RowScope.EntityRow(node: SchemaTreeNode.Entity, isExpanded: Boolean) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    ExpandIndicator(isExpanded)
    Text(text = node.info.name, style = typography.titleSmall, color = colors.textPrimary)
    Spacer(modifier = Modifier.weight(1f))
    Text(text = node.info.tableName, style = typography.codeMedium, color = colors.textMuted)
    Spacer(modifier = Modifier.width(MercurySpacing.sm))
}

@Composable
private fun RowScope.ColumnRow(node: SchemaTreeNode.Column) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors
    val col = node.info

    Text(
        text = col.name,
        style = typography.bodyMedium,
        color = colors.textPrimary,
        modifier = Modifier.weight(0.35f)
    )
    Text(
        text = if (col.typeConverter != null) "${col.typeConverter!!.fromType}\u2192${col.typeConverter!!.toType}" else col.type,
        style = typography.codeMedium,
        color = colors.textSecondary,
        modifier = Modifier.weight(0.3f)
    )
    Row(
        modifier = Modifier.weight(0.35f),
        horizontalArrangement = Arrangement.End
    ) {
        if (node.isPrimaryKey) {
            val pkText = if (node.autoGenerate) "PK \u00B7 auto" else "PK"
            MercuryBadge(pkText)
            Spacer(modifier = Modifier.width(MercurySpacing.xs))
        }
        if (node.isUnique) {
            MercuryBadge("unique")
            Spacer(modifier = Modifier.width(MercurySpacing.xs))
        }
        if (col.typeConverter != null) {
            MercuryBadge("converter")
        }
    }
}

@Composable
private fun RowScope.IndexRow(node: SchemaTreeNode.Index) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    Text(
        text = node.info.name,
        style = typography.codeMedium,
        color = colors.textSecondary,
        modifier = Modifier.weight(1f)
    )
    if (node.info.isUnique) {
        MercuryBadge("unique")
        Spacer(modifier = Modifier.width(MercurySpacing.sm))
    }
}

@Composable
private fun RowScope.ForeignKeyRow(node: SchemaTreeNode.ForeignKey) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors
    val fk = node.info

    Text(
        text = "${fk.childColumns.joinToString()} \u2192 ${fk.parentEntity}.${fk.parentColumns.joinToString()}",
        style = typography.codeMedium,
        color = colors.textSecondary,
        modifier = Modifier.weight(1f)
    )
    if (fk.onDelete != ForeignKeyAction.NO_ACTION) {
        MercuryBadge(fk.onDelete.name)
        Spacer(modifier = Modifier.width(MercurySpacing.sm))
    }
}

@Composable
private fun ViewRow(node: SchemaTreeNode.View, isExpanded: Boolean) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    ExpandIndicator(isExpanded)
    Text(text = node.info.name, style = typography.titleSmall, color = colors.textPrimary)
}

@Composable
private fun RowScope.TypeConverterRow(node: SchemaTreeNode.TypeConverter) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    Text(
        text = node.info.name,
        style = typography.bodyMedium,
        color = colors.textPrimary,
        modifier = Modifier.weight(0.5f)
    )
    Text(
        text = "${node.info.fromType} \u2194 ${node.info.toType}",
        style = typography.codeMedium,
        color = colors.textSecondary,
        modifier = Modifier.weight(0.5f)
    )
}

@Composable
private fun DaoRow(node: SchemaTreeNode.Dao, isExpanded: Boolean) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    ExpandIndicator(isExpanded)
    Text(text = node.info.name, style = typography.titleSmall, color = colors.textPrimary)
}

@Composable
private fun RowScope.DaoMethodRow(node: SchemaTreeNode.DaoMethodNode) {
    val typography = MercuryTheme.typography
    val colors = MercuryTheme.colors

    Text(
        text = node.method.name,
        style = typography.bodyMedium,
        color = colors.textPrimary,
        modifier = Modifier.weight(0.4f)
    )
    if (node.method.query != null) {
        Text(
            text = node.method.query!!.take(40),
            style = typography.codeSmall,
            color = colors.textMuted,
            modifier = Modifier.weight(0.4f)
        )
    }
    if (node.method.query == null) {
        Spacer(modifier = Modifier.weight(0.4f))
    }
    Row(
        modifier = Modifier.weight(0.2f),
        horizontalArrangement = Arrangement.End
    ) {
        MercuryBadge(node.method.type.name)
        Spacer(modifier = Modifier.width(MercurySpacing.sm))
    }
}

@Composable
private fun DividerRow(node: SchemaTreeNode.SectionDivider, modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = MercurySpacing.sm)
            .padding(start = (node.depth * MercurySize.treeIndent.value).dp)
            .padding(vertical = MercurySpacing.xs)
    ) {
        MercuryDottedDivider(label = node.label)
    }
}
