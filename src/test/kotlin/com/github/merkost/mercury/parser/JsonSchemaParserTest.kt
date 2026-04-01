package com.github.merkost.mercury.parser

import com.github.merkost.mercury.model.*
import org.junit.Assert.*
import org.junit.Test

class JsonSchemaParserTest {

    private val sampleJson = """
    {
      "formatVersion": 1,
      "database": {
        "version": 3,
        "identityHash": "abc123",
        "entities": [
          {
            "tableName": "users",
            "createSql": "CREATE TABLE users ...",
            "fields": [
              { "fieldPath": "id", "columnName": "id", "affinity": "INTEGER", "notNull": true },
              { "fieldPath": "name", "columnName": "name", "affinity": "TEXT", "notNull": true },
              { "fieldPath": "email", "columnName": "email", "affinity": "TEXT", "notNull": false }
            ],
            "primaryKey": { "autoGenerate": true, "columnNames": ["id"] },
            "indices": [
              { "name": "idx_email", "unique": true, "columnNames": ["email"], "orders": [], "createSql": "" }
            ],
            "foreignKeys": []
          },
          {
            "tableName": "orders",
            "createSql": "CREATE TABLE orders ...",
            "fields": [
              { "fieldPath": "id", "columnName": "id", "affinity": "INTEGER", "notNull": true },
              { "fieldPath": "userId", "columnName": "user_id", "affinity": "INTEGER", "notNull": true }
            ],
            "primaryKey": { "autoGenerate": true, "columnNames": ["id"] },
            "indices": [],
            "foreignKeys": [
              { "table": "users", "onDelete": "CASCADE", "onUpdate": "NO ACTION", "columns": ["user_id"], "referencedColumns": ["id"] }
            ]
          }
        ],
        "views": []
      }
    }
    """.trimIndent()

    @Test fun parsesVersion() {
        val db = JsonSchemaParser.parse(sampleJson)
        assertNotNull(db)
        assertEquals(3, db!!.version)
    }

    @Test fun parsesEntities() {
        val db = JsonSchemaParser.parse(sampleJson)!!
        assertEquals(2, db.entities.size)
        assertEquals("users", db.entities[0].tableName)
        assertEquals("orders", db.entities[1].tableName)
    }

    @Test fun parsesColumns() {
        val db = JsonSchemaParser.parse(sampleJson)!!
        val users = db.entities[0]
        assertEquals(3, users.columns.size)
        assertEquals("id", users.columns[0].columnName)
        assertEquals("INTEGER", users.columns[0].type)
        assertFalse(users.columns[0].isNullable)
        assertTrue(users.columns[2].isNullable)
    }

    @Test fun parsesPrimaryKey() {
        val db = JsonSchemaParser.parse(sampleJson)!!
        assertEquals(listOf("id"), db.entities[0].primaryKey.columnNames)
        assertTrue(db.entities[0].primaryKey.autoGenerate)
    }

    @Test fun parsesIndices() {
        val db = JsonSchemaParser.parse(sampleJson)!!
        assertEquals(1, db.entities[0].indices.size)
        assertEquals("idx_email", db.entities[0].indices[0].name)
        assertTrue(db.entities[0].indices[0].isUnique)
    }

    @Test fun parsesForeignKeys() {
        val db = JsonSchemaParser.parse(sampleJson)!!
        val orders = db.entities[1]
        assertEquals(1, orders.foreignKeys.size)
        assertEquals("Users", orders.foreignKeys[0].parentEntity)
        assertEquals(listOf("user_id"), orders.foreignKeys[0].childColumns)
        assertEquals(listOf("id"), orders.foreignKeys[0].parentColumns)
        assertEquals(ForeignKeyAction.CASCADE, orders.foreignKeys[0].onDelete)
    }

    @Test fun parsesEntityName() {
        val db = JsonSchemaParser.parse(sampleJson)!!
        assertEquals("Users", db.entities[0].name)
        assertEquals("Orders", db.entities[1].name)
    }

    @Test fun invalidJsonReturnsNull() {
        assertNull(JsonSchemaParser.parse("not json"))
    }
}
