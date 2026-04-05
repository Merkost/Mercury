package com.github.merkost.mercury.parser

import kotlinx.serialization.Serializable

@Serializable
data class RoomSchemaJson(
    val formatVersion: Int = 1,
    val database: RoomDatabaseJson
)

@Serializable
data class RoomDatabaseJson(
    val version: Int,
    val identityHash: String = "",
    val entities: List<RoomEntityJson> = emptyList(),
    val views: List<RoomViewJson> = emptyList()
)

@Serializable
data class RoomEntityJson(
    val tableName: String,
    val createSql: String = "",
    val fields: List<RoomFieldJson> = emptyList(),
    val primaryKey: RoomPrimaryKeyJson = RoomPrimaryKeyJson(),
    val indices: List<RoomIndexJson> = emptyList(),
    val foreignKeys: List<RoomForeignKeyJson> = emptyList()
)

@Serializable
data class RoomFieldJson(
    val fieldPath: String = "",
    val columnName: String,
    val affinity: String = "TEXT",
    val notNull: Boolean = true
)

@Serializable
data class RoomPrimaryKeyJson(
    val autoGenerate: Boolean = false,
    val columnNames: List<String> = emptyList()
)

@Serializable
data class RoomIndexJson(
    val name: String = "",
    val unique: Boolean = false,
    val columnNames: List<String> = emptyList(),
    val orders: List<String> = emptyList(),
    val createSql: String = ""
)

@Serializable
data class RoomForeignKeyJson(
    val table: String,
    val onDelete: String = "NO ACTION",
    val onUpdate: String = "NO ACTION",
    val columns: List<String> = emptyList(),
    val referencedColumns: List<String> = emptyList()
)

@Serializable
data class RoomViewJson(
    val viewName: String = "",
    val createSql: String = ""
)
