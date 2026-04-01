package com.github.merkost.mercury.ui.diagram

import androidx.compose.ui.geometry.Offset
import com.github.merkost.mercury.model.EntityInfo
import kotlin.math.ceil
import kotlin.math.sqrt

object DiagramLayoutEngine {

    fun computePositions(
        entities: List<EntityInfo>,
        cellWidth: Float,
        cellHeight: Float
    ): Map<String, Offset> {
        if (entities.isEmpty()) return emptyMap()

        val sorted = sortByConnectivity(entities)
        val cols = gridColumns(sorted.size)
        val positions = mutableMapOf<String, Offset>()
        val occupied = mutableSetOf<Pair<Int, Int>>()

        for (entity in sorted) {
            val preferredNeighbor = findMostConnectedPlacedNeighbor(entity, positions, entities, cellWidth, cellHeight)
            val cell = if (preferredNeighbor != null) {
                findAdjacentCell(preferredNeighbor, occupied, cols)
            } else {
                findNextFreeCell(occupied, cols)
            }
            occupied.add(cell)
            positions[entity.qualifiedName] = Offset(cell.first * cellWidth, cell.second * cellHeight)
        }

        return positions
    }

    fun gridColumns(entityCount: Int): Int = ceil(sqrt(entityCount.toDouble())).toInt().coerceAtLeast(1)

    private fun sortByConnectivity(entities: List<EntityInfo>): List<EntityInfo> {
        val connectionCount = mutableMapOf<String, Int>()
        val entityNames = entities.map { it.name }.toSet()

        for (entity in entities) {
            for (fk in entity.foreignKeys) {
                if (fk.parentEntity in entityNames) {
                    connectionCount[entity.qualifiedName] = (connectionCount[entity.qualifiedName] ?: 0) + 1
                    val parent = entities.find { it.name == fk.parentEntity }
                    if (parent != null) {
                        connectionCount[parent.qualifiedName] = (connectionCount[parent.qualifiedName] ?: 0) + 1
                    }
                }
            }
        }

        return entities.sortedByDescending { connectionCount[it.qualifiedName] ?: 0 }
    }

    private fun findMostConnectedPlacedNeighbor(
        entity: EntityInfo,
        placedPositions: Map<String, Offset>,
        allEntities: List<EntityInfo>,
        cellWidth: Float,
        cellHeight: Float
    ): Pair<Int, Int>? {
        val parentNames = entity.foreignKeys.map { it.parentEntity }
        val childEntities = allEntities.filter { other ->
            other.foreignKeys.any { it.parentEntity == entity.name }
        }
        val connectedNames = parentNames.mapNotNull { name ->
            allEntities.find { it.name == name }?.qualifiedName
        } + childEntities.map { it.qualifiedName }

        for (connectedQn in connectedNames) {
            val pos = placedPositions[connectedQn] ?: continue
            val col = if (cellWidth == 0f) 0 else (pos.x / cellWidth).toInt()
            val row = if (cellHeight == 0f) 0 else (pos.y / cellHeight).toInt()
            return Pair(col, row)
        }
        return null
    }

    private fun findAdjacentCell(
        neighbor: Pair<Int, Int>,
        occupied: Set<Pair<Int, Int>>,
        maxCols: Int
    ): Pair<Int, Int> {
        val candidates = listOf(
            Pair(neighbor.first + 1, neighbor.second),
            Pair(neighbor.first, neighbor.second + 1),
            Pair(neighbor.first - 1, neighbor.second),
            Pair(neighbor.first + 1, neighbor.second + 1),
            Pair(neighbor.first - 1, neighbor.second + 1),
            Pair(neighbor.first, neighbor.second - 1)
        ).filter { it.first in 0 until maxCols && it.second >= 0 }

        return candidates.firstOrNull { it !in occupied }
            ?: findNextFreeCell(occupied, maxCols)
    }

    private fun findNextFreeCell(occupied: Set<Pair<Int, Int>>, maxCols: Int): Pair<Int, Int> {
        var row = 0
        while (true) {
            for (col in 0 until maxCols) {
                val cell = Pair(col, row)
                if (cell !in occupied) return cell
            }
            row++
        }
    }
}
