package com.github.merkost.mercury.ui.tree

import com.github.merkost.mercury.model.*

enum class SectionType { ENTITIES, VIEWS, TYPE_CONVERTERS, DAOS }

sealed interface SchemaTreeNode {
    val nodeKey: String
    val depth: Int
    val qualifiedName: String?

    data class Database(
        val info: DatabaseInfo
    ) : SchemaTreeNode {
        override val nodeKey = "db:${info.qualifiedName}"
        override val depth = 0
        override val qualifiedName = info.qualifiedName
    }

    data class SectionHeader(
        val sectionType: SectionType,
        val count: Int,
        val parentDbKey: String
    ) : SchemaTreeNode {
        override val nodeKey = "section:$parentDbKey:$sectionType"
        override val depth = 1
        override val qualifiedName: String? = null
    }

    data class Entity(
        val info: EntityInfo
    ) : SchemaTreeNode {
        override val nodeKey = "entity:${info.qualifiedName}"
        override val depth = 2
        override val qualifiedName = info.qualifiedName
    }

    data class Column(
        val info: ColumnInfo,
        val isPrimaryKey: Boolean,
        val isUnique: Boolean,
        val autoGenerate: Boolean,
        val parentQualifiedName: String
    ) : SchemaTreeNode {
        override val nodeKey = "column:$parentQualifiedName:${info.name}"
        override val depth = 3
        override val qualifiedName = parentQualifiedName
    }

    data class SectionDivider(
        val label: String,
        val parentKey: String
    ) : SchemaTreeNode {
        override val nodeKey = "divider:$parentKey:$label"
        override val depth = 3
        override val qualifiedName: String? = null
    }

    data class Index(
        val info: IndexInfo,
        val parentQualifiedName: String
    ) : SchemaTreeNode {
        override val nodeKey = "index:$parentQualifiedName:${info.name}"
        override val depth = 3
        override val qualifiedName = parentQualifiedName
    }

    data class ForeignKey(
        val info: ForeignKeyInfo,
        val parentQualifiedName: String
    ) : SchemaTreeNode {
        override val nodeKey = "fk:$parentQualifiedName:${info.childColumns.joinToString()}"
        override val depth = 3
        override val qualifiedName = parentQualifiedName
    }

    data class View(
        val info: DatabaseViewInfo
    ) : SchemaTreeNode {
        override val nodeKey = "view:${info.qualifiedName}"
        override val depth = 2
        override val qualifiedName = info.qualifiedName
    }

    data class TypeConverter(
        val info: TypeConverterInfo
    ) : SchemaTreeNode {
        override val nodeKey = "converter:${info.qualifiedName}"
        override val depth = 2
        override val qualifiedName = info.qualifiedName
    }

    data class Dao(
        val info: DaoInfo
    ) : SchemaTreeNode {
        override val nodeKey = "dao:${info.qualifiedName}"
        override val depth = 2
        override val qualifiedName = info.qualifiedName
    }

    data class DaoMethodNode(
        val method: DaoMethod,
        val parentQualifiedName: String
    ) : SchemaTreeNode {
        override val nodeKey = "method:$parentQualifiedName:${method.name}"
        override val depth = 3
        override val qualifiedName = parentQualifiedName
    }
}

val SchemaTreeNode.isExpandable: Boolean
    get() = this is SchemaTreeNode.Database ||
            this is SchemaTreeNode.SectionHeader ||
            this is SchemaTreeNode.Entity ||
            this is SchemaTreeNode.View ||
            this is SchemaTreeNode.Dao
