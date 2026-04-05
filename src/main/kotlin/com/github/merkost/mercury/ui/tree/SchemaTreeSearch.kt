package com.github.merkost.mercury.ui.tree

fun fuzzyMatch(query: String, text: String): Boolean {
    val lowerQuery = query.lowercase()
    val lowerText = text.lowercase()
    var queryIndex = 0
    for (char in lowerText) {
        if (queryIndex < lowerQuery.length && char == lowerQuery[queryIndex]) {
            queryIndex++
        }
    }
    return queryIndex == lowerQuery.length
}

fun filterTreeNodes(
    nodes: List<SchemaTreeNode>,
    query: String
): List<SchemaTreeNode> {
    if (query.isBlank()) return nodes

    val matchingIndices = mutableSetOf<Int>()

    for (i in nodes.indices) {
        if (nodeMatchesQuery(nodes[i], query)) {
            matchingIndices.add(i)
            addAncestors(nodes, i, matchingIndices)
        }
    }

    return nodes.filterIndexed { index, _ -> index in matchingIndices }
}

private fun nodeMatchesQuery(node: SchemaTreeNode, query: String): Boolean {
    val searchableText = when (node) {
        is SchemaTreeNode.Entity -> "${node.info.name} ${node.info.tableName}"
        is SchemaTreeNode.Column -> "${node.info.name} ${node.info.columnName} ${node.info.type}"
        is SchemaTreeNode.Dao -> node.info.name
        is SchemaTreeNode.DaoMethodNode -> "${node.method.name} ${node.method.query.orEmpty()}"
        is SchemaTreeNode.View -> "${node.info.name} ${node.info.viewName}"
        is SchemaTreeNode.TypeConverter -> "${node.info.name} ${node.info.fromType} ${node.info.toType}"
        is SchemaTreeNode.Index -> "${node.info.name} ${node.info.columnNames.joinToString()}"
        is SchemaTreeNode.ForeignKey -> "${node.info.parentEntity} ${node.info.childColumns.joinToString()}"
        is SchemaTreeNode.Database -> node.info.name
        is SchemaTreeNode.SectionHeader -> ""
        is SchemaTreeNode.SectionDivider -> ""
    }
    return searchableText.isNotEmpty() && fuzzyMatch(query, searchableText)
}

private fun addAncestors(
    nodes: List<SchemaTreeNode>,
    childIndex: Int,
    result: MutableSet<Int>
) {
    val childDepth = nodes[childIndex].depth
    var currentDepth = childDepth

    for (i in (childIndex - 1) downTo 0) {
        val nodeDepth = nodes[i].depth
        if (nodeDepth < currentDepth) {
            result.add(i)
            currentDepth = nodeDepth
            if (nodeDepth == 0) break
        }
    }
}
