package com.github.merkost.mercury.parser

import com.github.merkost.mercury.model.*
import kotlinx.serialization.json.Json

object JsonSchemaParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(jsonContent: String): DatabaseInfo? {
        return try {
            val schema = json.decodeFromString<RoomSchemaJson>(jsonContent)
            mapToDatabase(schema)
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToDatabase(schema: RoomSchemaJson): DatabaseInfo {
        val db = schema.database
        return DatabaseInfo(
            name = "v${db.version}",
            qualifiedName = "schema.v${db.version}",
            version = db.version,
            entities = db.entities.map { mapToEntity(it) },
            views = db.views.map { mapToView(it) },
            typeConverters = emptyList(),
            daos = emptyList()
        )
    }

    private fun mapToEntity(entity: RoomEntityJson): EntityInfo {
        return EntityInfo(
            name = tableToPascalCase(entity.tableName),
            tableName = entity.tableName,
            qualifiedName = "schema.${tableToPascalCase(entity.tableName)}",
            columns = entity.fields.map { mapToColumn(it) },
            primaryKey = PrimaryKeyInfo(
                columnNames = entity.primaryKey.columnNames,
                autoGenerate = entity.primaryKey.autoGenerate
            ),
            foreignKeys = entity.foreignKeys.map { mapToForeignKey(it) },
            indices = entity.indices.map { mapToIndex(it) }
        )
    }

    private fun mapToColumn(field: RoomFieldJson): ColumnInfo {
        return ColumnInfo(
            name = field.fieldPath.ifEmpty { field.columnName },
            columnName = field.columnName,
            type = field.affinity,
            isNullable = !field.notNull
        )
    }

    private fun mapToForeignKey(fk: RoomForeignKeyJson): ForeignKeyInfo {
        return ForeignKeyInfo(
            parentEntity = tableToPascalCase(fk.table),
            parentColumns = fk.referencedColumns,
            childColumns = fk.columns,
            onDelete = mapFkAction(fk.onDelete),
            onUpdate = mapFkAction(fk.onUpdate)
        )
    }

    private fun mapToIndex(index: RoomIndexJson): IndexInfo {
        return IndexInfo(
            name = index.name,
            columnNames = index.columnNames,
            isUnique = index.unique
        )
    }

    private fun mapToView(view: RoomViewJson): DatabaseViewInfo {
        return DatabaseViewInfo(
            name = tableToPascalCase(view.viewName),
            viewName = view.viewName,
            qualifiedName = "schema.${tableToPascalCase(view.viewName)}",
            query = view.createSql
        )
    }

    private fun mapFkAction(action: String): ForeignKeyAction = when (action.uppercase()) {
        "CASCADE" -> ForeignKeyAction.CASCADE
        "RESTRICT" -> ForeignKeyAction.RESTRICT
        "SET_NULL" -> ForeignKeyAction.SET_NULL
        "SET_DEFAULT" -> ForeignKeyAction.SET_DEFAULT
        else -> ForeignKeyAction.NO_ACTION
    }

    private fun tableToPascalCase(tableName: String): String =
        tableName.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
}
