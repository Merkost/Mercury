package com.github.merkost.mercury.ui.comparison

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class ComparisonState {
    var leftVersion by mutableStateOf("")
        private set
    var rightVersion by mutableStateOf("")
        private set
    var expandedEntities by mutableStateOf(emptySet<String>())
        private set

    fun selectLeft(version: String) { leftVersion = version }
    fun selectRight(version: String) { rightVersion = version }

    fun swap() {
        val temp = leftVersion
        leftVersion = rightVersion
        rightVersion = temp
    }

    fun autoSelect(versionLabels: List<String>) {
        if (leftVersion.isNotEmpty() || rightVersion.isNotEmpty()) return
        if (versionLabels.size >= 2) {
            leftVersion = versionLabels[versionLabels.size - 2]
            rightVersion = versionLabels.last()
        } else if (versionLabels.isNotEmpty()) {
            leftVersion = versionLabels.first()
            rightVersion = versionLabels.first()
        }
    }

    fun toggleEntity(entityName: String) {
        expandedEntities = if (entityName in expandedEntities) expandedEntities - entityName else expandedEntities + entityName
    }

    fun isEntityExpanded(entityName: String): Boolean = entityName in expandedEntities
}

@Composable
fun rememberComparisonState(): ComparisonState = remember { ComparisonState() }
