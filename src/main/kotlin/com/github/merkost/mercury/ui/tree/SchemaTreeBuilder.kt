package com.github.merkost.mercury.ui.tree

import com.github.merkost.mercury.model.*

fun buildTreeNodes(
    database: DatabaseInfo,
    expandedKeys: Set<String>
): List<SchemaTreeNode> {
    val nodes = mutableListOf<SchemaTreeNode>()
    val dbNode = SchemaTreeNode.Database(database)
    nodes.add(dbNode)

    if (dbNode.nodeKey !in expandedKeys) return nodes

    addEntitiesSection(nodes, database, expandedKeys)
    addViewsSection(nodes, database, expandedKeys)
    addTypeConvertersSection(nodes, database, expandedKeys)
    addDaosSection(nodes, database, expandedKeys)

    return nodes
}

private fun addEntitiesSection(
    nodes: MutableList<SchemaTreeNode>,
    database: DatabaseInfo,
    expandedKeys: Set<String>
) {
    if (database.entities.isEmpty()) return
    val sectionNode = SchemaTreeNode.SectionHeader(
        SectionType.ENTITIES, database.entities.size, database.qualifiedName
    )
    nodes.add(sectionNode)

    if (sectionNode.nodeKey !in expandedKeys) return

    for (entity in database.entities) {
        addEntityNodes(nodes, entity, database, expandedKeys)
    }
}

private fun addEntityNodes(
    nodes: MutableList<SchemaTreeNode>,
    entity: EntityInfo,
    database: DatabaseInfo,
    expandedKeys: Set<String>
) {
    val entityNode = SchemaTreeNode.Entity(entity)
    nodes.add(entityNode)

    if (entityNode.nodeKey !in expandedKeys) return

    for (column in entity.columns) {
        val isPk = entity.primaryKey.columnNames.contains(column.columnName)
        val isUnique = entity.indices.any { it.isUnique && it.columnNames == listOf(column.columnName) }
        nodes.add(
            SchemaTreeNode.Column(
                info = column,
                isPrimaryKey = isPk,
                isUnique = isUnique,
                autoGenerate = isPk && entity.primaryKey.autoGenerate,
                parentQualifiedName = entity.qualifiedName
            )
        )
    }

    if (entity.indices.isNotEmpty()) {
        nodes.add(SchemaTreeNode.SectionDivider("indices", entityNode.nodeKey))
        for (index in entity.indices) {
            nodes.add(SchemaTreeNode.Index(index, entity.qualifiedName))
        }
    }

    if (entity.foreignKeys.isNotEmpty()) {
        nodes.add(SchemaTreeNode.SectionDivider("foreign keys", entityNode.nodeKey))
        for (fk in entity.foreignKeys) {
            nodes.add(SchemaTreeNode.ForeignKey(fk, entity.qualifiedName))
        }
    }
}

private fun addViewsSection(
    nodes: MutableList<SchemaTreeNode>,
    database: DatabaseInfo,
    expandedKeys: Set<String>
) {
    if (database.views.isEmpty()) return
    val sectionNode = SchemaTreeNode.SectionHeader(
        SectionType.VIEWS, database.views.size, database.qualifiedName
    )
    nodes.add(sectionNode)

    if (sectionNode.nodeKey !in expandedKeys) return

    for (view in database.views) {
        nodes.add(SchemaTreeNode.View(view))
    }
}

private fun addTypeConvertersSection(
    nodes: MutableList<SchemaTreeNode>,
    database: DatabaseInfo,
    expandedKeys: Set<String>
) {
    if (database.typeConverters.isEmpty()) return
    val sectionNode = SchemaTreeNode.SectionHeader(
        SectionType.TYPE_CONVERTERS, database.typeConverters.size, database.qualifiedName
    )
    nodes.add(sectionNode)

    if (sectionNode.nodeKey !in expandedKeys) return

    for (converter in database.typeConverters) {
        nodes.add(SchemaTreeNode.TypeConverter(converter))
    }
}

private fun addDaosSection(
    nodes: MutableList<SchemaTreeNode>,
    database: DatabaseInfo,
    expandedKeys: Set<String>
) {
    if (database.daos.isEmpty()) return
    val sectionNode = SchemaTreeNode.SectionHeader(
        SectionType.DAOS, database.daos.size, database.qualifiedName
    )
    nodes.add(sectionNode)

    if (sectionNode.nodeKey !in expandedKeys) return

    for (dao in database.daos) {
        val daoNode = SchemaTreeNode.Dao(dao)
        nodes.add(daoNode)

        if (daoNode.nodeKey !in expandedKeys) continue

        for (method in dao.methods) {
            nodes.add(SchemaTreeNode.DaoMethodNode(method, dao.qualifiedName))
        }
    }
}
