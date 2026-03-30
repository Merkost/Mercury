package com.github.merkost.mercury.model

import org.junit.Assert.*
import org.junit.Test

class RoomSchemaTest {

    @Test
    fun `entity with primary key and columns`() {
        val entity = EntityInfo(
            name = "UserEntity",
            tableName = "users",
            qualifiedName = "com.example.UserEntity",
            columns = listOf(
                ColumnInfo(name = "id", columnName = "id", type = "Long", isNullable = false),
                ColumnInfo(name = "name", columnName = "user_name", type = "String", isNullable = false),
                ColumnInfo(name = "email", columnName = "email", type = "String", isNullable = true)
            ),
            primaryKey = PrimaryKeyInfo(columnNames = listOf("id"), autoGenerate = true),
            foreignKeys = emptyList(),
            indices = listOf(
                IndexInfo(name = "idx_user_email", columnNames = listOf("email"), isUnique = true)
            )
        )

        assertEquals("UserEntity", entity.name)
        assertEquals("users", entity.tableName)
        assertEquals(3, entity.columns.size)
        assertTrue(entity.primaryKey.autoGenerate)
        assertEquals(1, entity.indices.size)
        assertTrue(entity.indices.first().isUnique)
    }

    @Test
    fun `entity with foreign key`() {
        val fk = ForeignKeyInfo(
            parentEntity = "ProfileEntity",
            parentColumns = listOf("id"),
            childColumns = listOf("profile_id"),
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.NO_ACTION
        )

        assertEquals("ProfileEntity", fk.parentEntity)
        assertEquals(ForeignKeyAction.CASCADE, fk.onDelete)
    }

    @Test
    fun `column with type converter`() {
        val converter = TypeConverterInfo(
            name = "DateConverter",
            qualifiedName = "com.example.DateConverter",
            fromType = "Long",
            toType = "Date"
        )
        val column = ColumnInfo(
            name = "createdAt",
            columnName = "created_at",
            type = "Date",
            isNullable = false,
            typeConverter = converter
        )

        assertNotNull(column.typeConverter)
        assertEquals("Long", column.typeConverter!!.fromType)
        assertEquals("Date", column.typeConverter!!.toType)
    }

    @Test
    fun `database info aggregates entities and daos`() {
        val entity = EntityInfo(
            name = "UserEntity",
            tableName = "users",
            qualifiedName = "com.example.UserEntity",
            columns = emptyList(),
            primaryKey = PrimaryKeyInfo(columnNames = listOf("id")),
            foreignKeys = emptyList(),
            indices = emptyList()
        )

        val dao = DaoInfo(
            name = "UserDao",
            qualifiedName = "com.example.UserDao",
            methods = emptyList()
        )

        val db = DatabaseInfo(
            name = "AppDatabase",
            qualifiedName = "com.example.AppDatabase",
            version = 3,
            entities = listOf(entity),
            views = emptyList(),
            typeConverters = emptyList(),
            daos = listOf(dao)
        )

        assertEquals(3, db.version)
        assertEquals(1, db.entities.size)
        assertEquals(1, db.daos.size)
    }

    @Test
    fun `room schema holds multiple databases`() {
        val db1 = DatabaseInfo(
            name = "AppDatabase",
            qualifiedName = "com.example.AppDatabase",
            version = 1,
            entities = emptyList(),
            views = emptyList(),
            typeConverters = emptyList(),
            daos = emptyList()
        )
        val db2 = DatabaseInfo(
            name = "CacheDatabase",
            qualifiedName = "com.example.CacheDatabase",
            version = 1,
            entities = emptyList(),
            views = emptyList(),
            typeConverters = emptyList(),
            daos = emptyList()
        )

        val schema = RoomSchema(databases = listOf(db1, db2))
        assertEquals(2, schema.databases.size)
    }

    @Test
    fun `embedded column has prefix`() {
        val column = ColumnInfo(
            name = "address",
            columnName = "addr_street",
            type = "String",
            isNullable = false,
            isEmbedded = true,
            embeddedPrefix = "addr_"
        )

        assertTrue(column.isEmbedded)
        assertEquals("addr_", column.embeddedPrefix)
    }
}
