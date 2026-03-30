package com.github.merkost.mercury.model

import org.junit.Assert.*
import org.junit.Test

class SchemaDiffTest {

    private fun entity(name: String, vararg columns: Pair<String, String>): EntityInfo {
        return EntityInfo(
            name = name,
            tableName = name.lowercase(),
            qualifiedName = "com.example.$name",
            columns = columns.map { (n, t) ->
                ColumnInfo(name = n, columnName = n, type = t, isNullable = false)
            },
            primaryKey = PrimaryKeyInfo(columnNames = listOf("id")),
            foreignKeys = emptyList(),
            indices = emptyList()
        )
    }

    @Test
    fun `diff with identical entities`() {
        val match = EntityMatch(
            left = entity("User", "id" to "Long", "name" to "String"),
            right = entity("User", "id" to "Long", "name" to "String"),
            matchType = MatchType.IDENTICAL,
            columnDiffs = emptyList()
        )

        assertEquals(MatchType.IDENTICAL, match.matchType)
        assertTrue(match.columnDiffs.isEmpty())
    }

    @Test
    fun `diff with type changed column`() {
        val diff = ColumnDiff(
            columnName = "timestamp",
            leftType = "Date",
            rightType = "Long",
            diffType = ColumnDiffType.TYPE_CHANGED
        )

        assertEquals(ColumnDiffType.TYPE_CHANGED, diff.diffType)
        assertEquals("Date", diff.leftType)
        assertEquals("Long", diff.rightType)
    }

    @Test
    fun `schema diff summary`() {
        val schemaDiff = SchemaDiff(
            leftDatabase = "AppDatabase",
            rightDatabase = "CacheDatabase",
            matchedEntities = listOf(
                EntityMatch(
                    left = entity("Settings"),
                    right = entity("Settings"),
                    matchType = MatchType.IDENTICAL,
                    columnDiffs = emptyList()
                )
            ),
            leftOnly = listOf(entity("User")),
            rightOnly = listOf(entity("CacheEntry"))
        )

        assertEquals(1, schemaDiff.matchedEntities.size)
        assertEquals(1, schemaDiff.leftOnly.size)
        assertEquals(1, schemaDiff.rightOnly.size)
    }

    @Test
    fun `column only in left side`() {
        val diff = ColumnDiff(
            columnName = "avatar",
            leftType = "String",
            rightType = null,
            diffType = ColumnDiffType.LEFT_ONLY
        )

        assertEquals(ColumnDiffType.LEFT_ONLY, diff.diffType)
        assertNull(diff.rightType)
    }
}
