package com.github.merkost.mercury.ui.toolwindow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.github.merkost.mercury.MercuryBundle
import com.github.merkost.mercury.ui.components.MercuryDropdown
import com.github.merkost.mercury.ui.components.MercurySearchField
import com.github.merkost.mercury.ui.components.MercuryTab
import com.github.merkost.mercury.ui.components.MercuryTabBar
import com.github.merkost.mercury.ui.navigation.MercuryDestination
import com.github.merkost.mercury.ui.navigation.MercuryNavigator
import com.github.merkost.mercury.ui.navigation.rememberMercuryNavigator
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

private val TABS = listOf(
    MercuryTab("schema", "Schema"),
    MercuryTab("diagram", "Diagram"),
    MercuryTab("queries", "Queries"),
    MercuryTab("diff", "Diff")
)

@Composable
fun MercuryToolWindowPanel(
    databaseNames: List<String>,
    selectedDatabase: String,
    onDatabaseSelected: (String) -> Unit
) {
    val colors = MercuryTheme.colors
    val navigator = rememberMercuryNavigator(selectedDatabase)
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabId by remember { mutableStateOf("schema") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        MercuryHeader(
            databaseNames = databaseNames,
            selectedDatabase = selectedDatabase,
            onDatabaseSelected = onDatabaseSelected
        )

        MercurySearchField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = MercurySpacing.lg, vertical = MercurySpacing.sm)
        )

        MercuryTabBar(
            tabs = TABS,
            selectedTabId = selectedTabId,
            onTabSelected = { tabId ->
                selectedTabId = tabId
                val tab = when (tabId) {
                    "schema" -> MercuryDestination.Tab.Schema(selectedDatabase)
                    "diagram" -> MercuryDestination.Tab.Diagram(selectedDatabase)
                    "queries" -> MercuryDestination.Tab.Queries(selectedDatabase)
                    "diff" -> MercuryDestination.Tab.Comparison(selectedDatabase, "")
                    else -> MercuryDestination.Tab.Schema(selectedDatabase)
                }
                navigator.switchTab(tab)
            }
        )

        MercuryContentArea(
            selectedTabId = selectedTabId,
            navigator = navigator,
            searchQuery = searchQuery,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MercuryHeader(
    databaseNames: List<String>,
    selectedDatabase: String,
    onDatabaseSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MercurySpacing.lg)
    ) {
        if (databaseNames.isNotEmpty()) {
            MercuryDropdown(
                selectedValue = selectedDatabase,
                options = databaseNames,
                onOptionSelected = onDatabaseSelected
            )
        } else {
            Text(
                text = MercuryBundle.message("database.empty"),
                style = MercuryTheme.typography.bodyMedium,
                color = MercuryTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun MercuryContentArea(
    selectedTabId: String,
    @Suppress("UNUSED_PARAMETER") navigator: MercuryNavigator,
    @Suppress("UNUSED_PARAMETER") searchQuery: String,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(MercurySpacing.lg)
    ) {
        Text(
            text = "${selectedTabId.replaceFirstChar { it.uppercase() }} view — coming in Sprint 3",
            style = typography.bodyMedium,
            color = colors.textMuted
        )
    }
}
