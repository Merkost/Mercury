package com.github.merkost.mercury.ui.queries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class QueriesState {

    var expandedDaos by mutableStateOf(emptySet<String>())
        private set

    var expandedMethods by mutableStateOf(emptySet<String>())
        private set

    fun toggleDao(qualifiedName: String) {
        expandedDaos = if (qualifiedName in expandedDaos) {
            expandedDaos - qualifiedName
        } else {
            expandedDaos + qualifiedName
        }
    }

    fun toggleMethod(key: String) {
        expandedMethods = if (key in expandedMethods) {
            expandedMethods - key
        } else {
            expandedMethods + key
        }
    }

    fun isDaoExpanded(qualifiedName: String): Boolean = qualifiedName in expandedDaos

    fun isMethodExpanded(key: String): Boolean = key in expandedMethods

    fun expandFirstDao(qualifiedName: String) {
        if (expandedDaos.isEmpty()) {
            expandedDaos = setOf(qualifiedName)
        }
    }

    fun prune(validDaoKeys: Set<String>, validMethodKeys: Set<String>) {
        expandedDaos = expandedDaos.intersect(validDaoKeys)
        expandedMethods = expandedMethods.intersect(validMethodKeys)
    }
}

@Composable
fun rememberQueriesState(databaseQualifiedName: String): QueriesState {
    return remember(databaseQualifiedName) { QueriesState() }
}
