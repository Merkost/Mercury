package com.github.merkost.mercury.analysis

import com.github.merkost.mercury.model.*
import org.junit.Assert.*
import org.junit.Test

class SchemaDifferTest {

    private fun entity(name: String, columns: List<ColumnInfo> = listOf(
        ColumnInfo(name = "id", columnName = "id", type = "Long", isNullable = false)
    )) = EntityInfo(name = name, tableName = name.lowercase(), qualifiedName = "com.example.$name",
        columns = columns, primaryKey = PrimaryKeyInfo(columnNames = listOf("id")),
        foreignKeys = emptyList(), indices = emptyList())

    private fun db(name: String, entities: List<EntityInfo>) = DatabaseInfo(name = name,
        qualifiedName = "com.example.$name", version = 1, entities = entities,
        views = emptyList(), typeConverters = emptyList(), daos = emptyList())

    @Test fun identicalEntitiesMatchAsIdentical() {
        val diff = SchemaDiffer.compare(db("L", listOf(entity("User"))), db("R", listOf(entity("User"))))
        assertEquals(1, diff.matchedEntities.size)
        assertEquals(MatchType.IDENTICAL, diff.matchedEntities[0].matchType)
        assertTrue(diff.matchedEntities[0].columnDiffs.isEmpty())
    }

    @Test fun entitiesWithDifferentColumnsMatchAsSimilar() {
        val lc = listOf(ColumnInfo("id","id","Long",false), ColumnInfo("name","name","String",false))
        val rc = listOf(ColumnInfo("id","id","Long",false), ColumnInfo("name","name","Int",false))
        val diff = SchemaDiffer.compare(db("L", listOf(entity("User", lc))), db("R", listOf(entity("User", rc))))
        assertEquals(MatchType.SIMILAR, diff.matchedEntities[0].matchType)
        assertEquals(ColumnDiffType.TYPE_CHANGED, diff.matchedEntities[0].columnDiffs[0].diffType)
    }

    @Test fun leftOnlyEntityDetected() {
        val diff = SchemaDiffer.compare(db("L", listOf(entity("User"), entity("Pay"))), db("R", listOf(entity("User"))))
        assertEquals(1, diff.leftOnly.size)
        assertEquals("Pay", diff.leftOnly[0].name)
    }

    @Test fun rightOnlyEntityDetected() {
        val diff = SchemaDiffer.compare(db("L", listOf(entity("User"))), db("R", listOf(entity("User"), entity("Cart"))))
        assertEquals(1, diff.rightOnly.size)
        assertEquals("Cart", diff.rightOnly[0].name)
    }

    @Test fun nullabilityChangeDetected() {
        val lc = listOf(ColumnInfo("email","email","String",false))
        val rc = listOf(ColumnInfo("email","email","String",true))
        val diff = SchemaDiffer.compare(db("L", listOf(entity("User", lc))), db("R", listOf(entity("User", rc))))
        assertEquals(ColumnDiffType.NULLABILITY_CHANGED, diff.matchedEntities[0].columnDiffs[0].diffType)
    }

    @Test fun columnOnlyInLeftDetected() {
        val lc = listOf(ColumnInfo("id","id","Long",false), ColumnInfo("extra","extra","String",true))
        val rc = listOf(ColumnInfo("id","id","Long",false))
        val diff = SchemaDiffer.compare(db("L", listOf(entity("User", lc))), db("R", listOf(entity("User", rc))))
        assertEquals(ColumnDiffType.LEFT_ONLY, diff.matchedEntities[0].columnDiffs[0].diffType)
    }

    @Test fun emptyDatabasesProduceEmptyDiff() {
        val diff = SchemaDiffer.compare(db("L", emptyList()), db("R", emptyList()))
        assertTrue(diff.matchedEntities.isEmpty())
    }
}
