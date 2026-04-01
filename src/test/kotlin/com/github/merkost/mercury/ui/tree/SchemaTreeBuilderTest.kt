package com.github.merkost.mercury.ui.tree

import com.github.merkost.mercury.model.*
import org.junit.Assert.*
import org.junit.Test

class SchemaTreeBuilderTest {

    private val testEntity = EntityInfo(
        name = "UserEntity",
        tableName = "users",
        qualifiedName = "com.example.UserEntity",
        columns = listOf(
            ColumnInfo(name = "id", columnName = "id", type = "Long", isNullable = false),
            ColumnInfo(name = "name", columnName = "name", type = "String", isNullable = false)
        ),
        primaryKey = PrimaryKeyInfo(columnNames = listOf("id"), autoGenerate = true),
        foreignKeys = emptyList(),
        indices = listOf(IndexInfo(name = "idx_name", columnNames = listOf("name"), isUnique = true))
    )

    private val testDao = DaoInfo(
        name = "UserDao",
        qualifiedName = "com.example.UserDao",
        methods = listOf(
            DaoMethod(name = "getAll", type = DaoMethodType.QUERY, query = "SELECT * FROM users", returnType = "List<UserEntity>")
        )
    )

    private val testDb = DatabaseInfo(
        name = "AppDatabase",
        qualifiedName = "com.example.AppDatabase",
        version = 1,
        entities = listOf(testEntity),
        views = emptyList(),
        typeConverters = emptyList(),
        daos = listOf(testDao)
    )

    @Test
    fun `collapsed database shows only root node`() {
        val nodes = buildTreeNodes(testDb, emptySet())
        assertEquals(1, nodes.size)
        assertTrue(nodes[0] is SchemaTreeNode.Database)
    }

    @Test
    fun `expanded database shows section headers`() {
        val expanded = setOf("db:com.example.AppDatabase")
        val nodes = buildTreeNodes(testDb, expanded)
        assertTrue(nodes.size > 1)
        assertTrue(nodes.any { it is SchemaTreeNode.SectionHeader })
    }

    @Test
    fun `expanded entities section shows entity nodes`() {
        val expanded = setOf(
            "db:com.example.AppDatabase",
            "section:com.example.AppDatabase:ENTITIES"
        )
        val nodes = buildTreeNodes(testDb, expanded)
        assertTrue(nodes.any { it is SchemaTreeNode.Entity })
    }

    @Test
    fun `expanded entity shows columns and indices`() {
        val expanded = setOf(
            "db:com.example.AppDatabase",
            "section:com.example.AppDatabase:ENTITIES",
            "entity:com.example.UserEntity"
        )
        val nodes = buildTreeNodes(testDb, expanded)
        val columns = nodes.filterIsInstance<SchemaTreeNode.Column>()
        assertEquals(2, columns.size)

        val idCol = columns.find { it.info.name == "id" }!!
        assertTrue(idCol.isPrimaryKey)
        assertTrue(idCol.autoGenerate)

        val nameCol = columns.find { it.info.name == "name" }!!
        assertTrue(nameCol.isUnique)

        assertTrue(nodes.any { it is SchemaTreeNode.SectionDivider && it.label == "indices" })
        assertTrue(nodes.any { it is SchemaTreeNode.Index })
    }

    @Test
    fun `node depths are correct`() {
        val expanded = setOf(
            "db:com.example.AppDatabase",
            "section:com.example.AppDatabase:ENTITIES",
            "entity:com.example.UserEntity"
        )
        val nodes = buildTreeNodes(testDb, expanded)
        nodes.forEach { node ->
            when (node) {
                is SchemaTreeNode.Database -> assertEquals(0, node.depth)
                is SchemaTreeNode.SectionHeader -> assertEquals(1, node.depth)
                is SchemaTreeNode.Entity -> assertEquals(2, node.depth)
                is SchemaTreeNode.Column -> assertEquals(3, node.depth)
                is SchemaTreeNode.SectionDivider -> assertEquals(3, node.depth)
                is SchemaTreeNode.Index -> assertEquals(3, node.depth)
                else -> {}
            }
        }
    }

    @Test
    fun `empty sections are not shown`() {
        val dbNoViews = testDb.copy(views = emptyList())
        val expanded = setOf("db:com.example.AppDatabase")
        val nodes = buildTreeNodes(dbNoViews, expanded)
        assertFalse(nodes.any { it is SchemaTreeNode.SectionHeader && it.sectionType == SectionType.VIEWS })
    }
}
