package com.github.merkost.mercury.ui.tree

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.intellij.openapi.project.Project
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.ui.navigation.PsiNavigationBridge

@Composable
fun SchemaTreeView(
    database: DatabaseInfo,
    searchQuery: String,
    project: Project,
    treeState: SchemaTreeState,
    modifier: Modifier = Modifier
) {
    val allNodes = remember(database, treeState.expandedKeys) {
        buildTreeNodes(database, treeState.expandedKeys)
    }

    val visibleNodes = remember(allNodes, searchQuery) {
        if (searchQuery.isBlank()) allNodes
        else filterTreeNodes(allNodes, searchQuery)
    }

    LaunchedEffect(allNodes) {
        val validKeys = allNodes.map { it.nodeKey }.toSet()
        treeState.pruneStaleKeys(validKeys)
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(visibleNodes, key = { it.nodeKey }) { node ->
            SchemaTreeRow(
                node = node,
                isSelected = treeState.selectedKey == node.nodeKey,
                isExpanded = treeState.isExpanded(node.nodeKey),
                onClick = {
                    treeState.select(node.nodeKey)
                    if (node.isExpandable) treeState.toggle(node.nodeKey)
                },
                onDoubleClick = {
                    PsiNavigationBridge.navigateToNode(project, node)
                }
            )
        }
    }
}
