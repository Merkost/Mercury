package com.github.merkost.mercury.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class MercuryNavigator(
    initialDestination: MercuryDestination
) {
    private val _backStack: SnapshotStateList<MercuryDestination> = mutableStateListOf(initialDestination)
    val backStack: List<MercuryDestination> get() = _backStack

    var currentTab: MercuryDestination.Tab by mutableStateOf(
        initialDestination as? MercuryDestination.Tab
            ?: MercuryDestination.Tab.Schema("")
    )
        private set

    val current: MercuryDestination
        get() = _backStack.lastOrNull() ?: currentTab

    val canGoBack: Boolean
        get() = _backStack.size > 1

    fun switchTab(tab: MercuryDestination.Tab) {
        currentTab = tab
        _backStack.clear()
        _backStack.add(tab)
    }

    fun navigateToDetail(detail: MercuryDestination.Detail) {
        _backStack.add(detail)
    }

    fun goBack(): Boolean {
        if (_backStack.size <= 1) return false
        _backStack.removeLast()
        return true
    }

    fun openEntity(databaseId: String, entityName: String) {
        navigateToDetail(MercuryDestination.Detail.Entity(databaseId, entityName))
    }

    fun openDao(databaseId: String, daoName: String) {
        navigateToDetail(MercuryDestination.Detail.Dao(databaseId, daoName))
    }

    fun openComparison(leftDbId: String, rightDbId: String) {
        switchTab(MercuryDestination.Tab.Comparison(leftDbId, rightDbId))
    }
}

@Composable
fun rememberMercuryNavigator(
    initialDatabaseId: String = ""
): MercuryNavigator {
    return remember(initialDatabaseId) {
        MercuryNavigator(MercuryDestination.Tab.Schema(initialDatabaseId))
    }
}
