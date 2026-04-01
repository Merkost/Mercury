package com.github.merkost.mercury.ui.diagram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme

data class RelationshipEdge(
    val childQualifiedName: String,
    val parentQualifiedName: String,
    val childColumnIndex: Int,
    val parentColumnIndex: Int
)

@Composable
fun RelationshipLines(
    database: DatabaseInfo,
    entityPositions: Map<String, Offset>,
    entitySizes: Map<String, Pair<Float, Float>>,
    selectedEntity: String?,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val edges = buildEdges(database)

    val defaultWidth = MercurySize.entityCardMaxWidth.value
    val defaultHeight = 200f

    Canvas(modifier = modifier.fillMaxSize()) {
        drawGridDots(colors.borderSubtle.copy(alpha = 0.3f), 32f)

        for (edge in edges) {
            val childPos = entityPositions[edge.childQualifiedName] ?: continue
            val parentPos = entityPositions[edge.parentQualifiedName] ?: continue
            val childSize = entitySizes[edge.childQualifiedName] ?: Pair(defaultWidth, defaultHeight)
            val parentSize = entitySizes[edge.parentQualifiedName] ?: Pair(defaultWidth, defaultHeight)

            val isHighlighted = selectedEntity != null &&
                (edge.childQualifiedName == selectedEntity || edge.parentQualifiedName == selectedEntity)
            val isDimmed = selectedEntity != null && !isHighlighted

            val lineColor = when {
                isHighlighted -> colors.diagramEdgeHighlight
                isDimmed -> colors.diagramEdge.copy(alpha = 0.4f)
                else -> colors.diagramEdge
            }
            val lineWidth = if (isHighlighted) {
                MercurySize.relationshipLineWidthHighlight.toPx()
            } else {
                MercurySize.relationshipLineWidth.toPx()
            }

            val headerPadding = MercurySpacing.sm.toPx() * 2
            val headerTextHeight = 16f
            val borderHeight = MercurySize.borderWidth.toPx()
            val fieldPadding = MercurySpacing.xs.toPx()
            val headerTotalHeight = headerPadding + headerTextHeight + borderHeight + fieldPadding
            val fieldHeight = MercurySize.entityFieldHeight.toPx()
            val childY = childPos.y + headerTotalHeight + (edge.childColumnIndex + 0.5f) * fieldHeight
            val parentY = parentPos.y + headerTotalHeight + (edge.parentColumnIndex + 0.5f) * fieldHeight

            val childExitsRight = parentPos.x > childPos.x
            val childX = if (childExitsRight) childPos.x + childSize.first else childPos.x
            val parentX = if (childExitsRight) parentPos.x else parentPos.x + parentSize.first

            val midX = (childX + parentX) / 2f

            drawLine(lineColor, Offset(childX, childY), Offset(midX, childY), lineWidth, StrokeCap.Round)
            drawLine(lineColor, Offset(midX, childY), Offset(midX, parentY), lineWidth, StrokeCap.Round)
            drawLine(lineColor, Offset(midX, parentY), Offset(parentX, parentY), lineWidth, StrokeCap.Round)

            drawArrowhead(
                tip = Offset(parentX, parentY),
                pointsRight = !childExitsRight,
                color = lineColor,
                size = 8f
            )

            drawCardinalityLabel(
                this,
                "N",
                Offset(childX + if (childExitsRight) 8f else -8f, childY - 12f),
                lineColor
            )
            drawCardinalityLabel(
                this,
                "1",
                Offset(parentX + if (!childExitsRight) 8f else -8f, parentY - 12f),
                lineColor
            )
        }
    }
}

private fun DrawScope.drawGridDots(color: Color, spacing: Float) {
    val cols = (size.width / spacing).toInt() + 1
    val rows = (size.height / spacing).toInt() + 1
    for (row in 0..rows) {
        for (col in 0..cols) {
            drawCircle(color, radius = 1f, center = Offset(col * spacing, row * spacing))
        }
    }
}

private fun DrawScope.drawArrowhead(tip: Offset, pointsRight: Boolean, color: Color, size: Float) {
    val direction = if (pointsRight) 1f else -1f
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(tip.x - direction * size, tip.y - size / 2f)
        lineTo(tip.x - direction * size, tip.y + size / 2f)
        close()
    }
    drawPath(path, color, style = Fill)
}

private fun drawCardinalityLabel(scope: DrawScope, label: String, position: Offset, color: Color) {
    val radius = if (label == "N") 3f else 2f
    scope.drawCircle(color, radius = radius, center = position)
    if (label == "N") {
        scope.drawCircle(color.copy(alpha = 0.5f), radius = radius + 2f, center = position)
    }
}

fun buildEdges(database: DatabaseInfo): List<RelationshipEdge> {
    val entityByName = database.entities.associateBy { it.name }
    val entityByTableName = database.entities.associateBy { it.tableName }

    return database.entities.flatMap { child ->
        child.foreignKeys.mapNotNull { fk ->
            val parent = entityByName[fk.parentEntity]
                ?: entityByTableName[fk.parentEntity]
                ?: return@mapNotNull null
            val childColIndex = child.columns.indexOfFirst { it.columnName in fk.childColumns }
            val parentColIndex = parent.columns.indexOfFirst { it.columnName in fk.parentColumns }
            if (childColIndex < 0 || parentColIndex < 0) return@mapNotNull null

            RelationshipEdge(
                childQualifiedName = child.qualifiedName,
                parentQualifiedName = parent.qualifiedName,
                childColumnIndex = childColIndex.coerceAtLeast(0),
                parentColumnIndex = parentColIndex.coerceAtLeast(0)
            )
        }
    }
}
