package com.github.merkost.mercury.ui.diagram

import androidx.compose.ui.geometry.Offset
import com.github.merkost.mercury.model.*
import org.junit.Assert.*
import org.junit.Test

class DiagramLayoutEngineTest {

    private fun entity(name: String, fks: List<ForeignKeyInfo> = emptyList()) = EntityInfo(
        name = name,
        tableName = name.lowercase(),
        qualifiedName = "com.example.$name",
        columns = listOf(ColumnInfo(name = "id", columnName = "id", type = "Long", isNullable = false)),
        primaryKey = PrimaryKeyInfo(columnNames = listOf("id")),
        foreignKeys = fks,
        indices = emptyList()
    )

    @Test
    fun singleEntityPlacedAtOrigin() {
        val positions = DiagramLayoutEngine.computePositions(
            entities = listOf(entity("User")),
            cellWidth = 200f,
            cellHeight = 150f
        )
        assertEquals(1, positions.size)
        assertEquals(Offset(0f, 0f), positions["com.example.User"])
    }

    @Test
    fun twoUnrelatedEntitiesPlacedInGrid() {
        val positions = DiagramLayoutEngine.computePositions(
            entities = listOf(entity("User"), entity("Product")),
            cellWidth = 200f,
            cellHeight = 150f
        )
        assertEquals(2, positions.size)
        assertNotNull(positions["com.example.User"])
        assertNotNull(positions["com.example.Product"])
        assertNotEquals(positions["com.example.User"], positions["com.example.Product"])
    }

    @Test
    fun relatedEntitiesPlacedAdjacently() {
        val fk = ForeignKeyInfo(
            parentEntity = "User",
            parentColumns = listOf("id"),
            childColumns = listOf("userId")
        )
        val entities = listOf(
            entity("User"),
            entity("Order", fks = listOf(fk)),
            entity("Product")
        )
        val positions = DiagramLayoutEngine.computePositions(entities, 200f, 150f)
        val userPos = positions["com.example.User"]!!
        val orderPos = positions["com.example.Order"]!!
        val distance = (userPos - orderPos).getDistance()
        val maxAdjacent = kotlin.math.sqrt(200f * 200f + 150f * 150f)
        assertTrue("Related entities should be adjacent", distance <= maxAdjacent + 1f)
    }

    @Test
    fun isDeterministic() {
        val entities = listOf(entity("A"), entity("B"), entity("C"))
        val first = DiagramLayoutEngine.computePositions(entities, 200f, 150f)
        val second = DiagramLayoutEngine.computePositions(entities, 200f, 150f)
        assertEquals(first, second)
    }

    @Test
    fun emptyListReturnsEmptyMap() {
        val positions = DiagramLayoutEngine.computePositions(emptyList(), 200f, 150f)
        assertTrue(positions.isEmpty())
    }

    @Test
    fun computeColumnsForEntityCount() {
        assertEquals(1, DiagramLayoutEngine.gridColumns(1))
        assertEquals(2, DiagramLayoutEngine.gridColumns(3))
        assertEquals(3, DiagramLayoutEngine.gridColumns(7))
        assertEquals(4, DiagramLayoutEngine.gridColumns(12))
        assertEquals(5, DiagramLayoutEngine.gridColumns(20))
    }
}
