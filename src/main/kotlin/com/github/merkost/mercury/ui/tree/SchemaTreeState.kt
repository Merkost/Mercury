package com.github.merkost.mercury.ui.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class SchemaTreeState(
    initialExpanded: Set<String> = emptySet()
) {
    var expandedKeys by mutableStateOf(initialExpanded)
        private set

    var selectedKey: String? by mutableStateOf(null)
        private set

    fun toggle(key: String) {
        expandedKeys = if (key in expandedKeys) {
            expandedKeys - key
        } else {
            expandedKeys + key
        }
    }

    fun expand(key: String) {
        expandedKeys = expandedKeys + key
    }

    fun select(key: String?) {
        selectedKey = key
    }

    fun isExpanded(key: String): Boolean = key in expandedKeys

    fun expandAll(keys: Collection<String>) {
        expandedKeys = expandedKeys + keys
    }

    fun collapseAll() {
        expandedKeys = emptySet()
    }

    fun pruneStaleKeys(validKeys: Set<String>) {
        expandedKeys = expandedKeys.intersect(validKeys)
        if (selectedKey != null && selectedKey !in validKeys) {
            selectedKey = null
        }
    }
}

@Composable
fun rememberSchemaTreeState(
    databaseQualifiedName: String
): SchemaTreeState {
    return remember(databaseQualifiedName) {
        val defaultExpanded = setOf(
            "db:$databaseQualifiedName",
            "section:$databaseQualifiedName:ENTITIES"
        )
        SchemaTreeState(initialExpanded = defaultExpanded)
    }
}
