package com.github.merkost.mercury.model

data class RoomSchema(
    val databases: List<DatabaseInfo>
)

data class DatabaseInfo(
    val name: String,
    val qualifiedName: String,
    val version: Int,
    val entities: List<EntityInfo>,
    val views: List<DatabaseViewInfo>,
    val typeConverters: List<TypeConverterInfo>,
    val daos: List<DaoInfo>,
    val sourceSet: String = "main"
)

data class EntityInfo(
    val name: String,
    val tableName: String,
    val qualifiedName: String,
    val columns: List<ColumnInfo>,
    val primaryKey: PrimaryKeyInfo,
    val foreignKeys: List<ForeignKeyInfo>,
    val indices: List<IndexInfo>,
    val sourceSet: String = "main"
)

data class ColumnInfo(
    val name: String,
    val columnName: String,
    val type: String,
    val isNullable: Boolean,
    val defaultValue: String? = null,
    val typeConverter: TypeConverterInfo? = null,
    val isEmbedded: Boolean = false,
    val embeddedPrefix: String? = null
)

data class PrimaryKeyInfo(
    val columnNames: List<String>,
    val autoGenerate: Boolean = false
)

data class ForeignKeyInfo(
    val parentEntity: String,
    val parentColumns: List<String>,
    val childColumns: List<String>,
    val onDelete: ForeignKeyAction = ForeignKeyAction.NO_ACTION,
    val onUpdate: ForeignKeyAction = ForeignKeyAction.NO_ACTION
)

enum class ForeignKeyAction {
    NO_ACTION,
    RESTRICT,
    SET_NULL,
    SET_DEFAULT,
    CASCADE
}

data class IndexInfo(
    val name: String,
    val columnNames: List<String>,
    val isUnique: Boolean = false,
    val orders: List<IndexOrder> = emptyList()
)

enum class IndexOrder {
    ASC,
    DESC
}
