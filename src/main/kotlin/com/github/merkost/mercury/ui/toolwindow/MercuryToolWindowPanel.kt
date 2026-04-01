package com.github.merkost.mercury.ui.toolwindow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.intellij.openapi.project.Project
import com.github.merkost.mercury.MercuryBundle
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.model.MercuryUiState
import com.github.merkost.mercury.model.RoomSchema
import com.github.merkost.mercury.ui.components.*
import com.github.merkost.mercury.ui.navigation.MercuryDestination
import com.github.merkost.mercury.ui.navigation.rememberMercuryNavigator
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import com.github.merkost.mercury.ui.diagram.DiagramScene
import com.github.merkost.mercury.ui.diagram.DiagramState
import com.github.merkost.mercury.ui.diagram.rememberDiagramState
import com.github.merkost.mercury.ui.queries.QueriesScene
import com.github.merkost.mercury.ui.queries.QueriesState
import com.github.merkost.mercury.ui.queries.rememberQueriesState
import com.github.merkost.mercury.ui.comparison.ComparisonScene
import com.github.merkost.mercury.ui.comparison.ComparisonState
import com.github.merkost.mercury.ui.comparison.rememberComparisonState
import com.github.merkost.mercury.ui.tree.SchemaTreeState
import com.github.merkost.mercury.ui.tree.SchemaTreeView
import com.github.merkost.mercury.ui.tree.rememberSchemaTreeState
import org.jetbrains.jewel.ui.component.CircularProgressIndicator

private val TABS
    @Composable get() = listOf(
        MercuryTab("schema", MercuryBundle.message("toolwindow.tab.schema")),
        MercuryTab("diagram", MercuryBundle.message("toolwindow.tab.diagram")),
        MercuryTab("queries", MercuryBundle.message("toolwindow.tab.queries")),
        MercuryTab("diff", MercuryBundle.message("toolwindow.tab.diff"))
    )

@Composable
fun MercuryToolWindowPanel(
    project: Project,
    uiState: MercuryUiState,
    onRefresh: () -> Unit
) {
    val colors = MercuryTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabId by remember { mutableStateOf("schema") }
    val navigator = rememberMercuryNavigator("")

    val schema: RoomSchema? = when (uiState) {
        is MercuryUiState.Populated -> uiState.schema
        is MercuryUiState.Refreshing -> uiState.currentSchema
        else -> null
    }
    val isRefreshing = uiState is MercuryUiState.Refreshing

    val databaseNames = schema?.databases?.map { it.name } ?: emptyList()
    var selectedDatabase by remember(databaseNames) {
        mutableStateOf(databaseNames.firstOrNull() ?: "")
    }
    val selectedDatabaseInfo = schema?.databases?.find { it.name == selectedDatabase }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        MercuryHeader(
            databaseNames = databaseNames,
            selectedDatabase = selectedDatabase,
            isRefreshing = isRefreshing,
            onDatabaseSelected = { selectedDatabase = it }
        )

        MercurySearchField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = MercurySpacing.xl, vertical = MercurySpacing.sm)
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

        val treeState = rememberSchemaTreeState(selectedDatabaseInfo?.qualifiedName ?: selectedDatabase)
        val diagramState = rememberDiagramState(selectedDatabaseInfo?.qualifiedName ?: selectedDatabase)
        val queriesState = rememberQueriesState(selectedDatabaseInfo?.qualifiedName ?: selectedDatabase)
        val comparisonState = rememberComparisonState()

        MercuryContentArea(
            project = project,
            selectedTabId = selectedTabId,
            database = selectedDatabaseInfo,
            searchQuery = searchQuery,
            treeState = treeState,
            diagramState = diagramState,
            queriesState = queriesState,
            comparisonState = comparisonState,
            schema = schema,
            uiState = uiState,
            onRefresh = onRefresh,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MercuryHeader(
    databaseNames: List<String>,
    selectedDatabase: String,
    isRefreshing: Boolean,
    onDatabaseSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MercurySpacing.xl, vertical = MercurySpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (databaseNames.isNotEmpty()) {
            MercuryDropdown(
                selectedValue = selectedDatabase,
                options = databaseNames,
                onOptionSelected = onDatabaseSelected
            )
            if (isRefreshing) {
                Spacer(Modifier.width(MercurySpacing.sm))
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun MercuryContentArea(
    project: Project,
    selectedTabId: String,
    database: DatabaseInfo?,
    searchQuery: String,
    treeState: SchemaTreeState,
    diagramState: DiagramState,
    queriesState: QueriesState,
    comparisonState: ComparisonState,
    schema: RoomSchema?,
    uiState: MercuryUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(top = MercurySpacing.sm)) {
        MercuryErrorBanner(
            message = if (uiState is MercuryUiState.Error) uiState.message else "",
            visible = uiState is MercuryUiState.Error,
            onRetry = onRefresh
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState is MercuryUiState.Loading -> {
                    MercurySkeleton()
                }
                uiState is MercuryUiState.Empty -> {
                    MercuryEmptyState()
                }
                selectedTabId == "schema" && database != null -> {
                    SchemaTreeView(
                        database = database,
                        searchQuery = searchQuery,
                        project = project,
                        treeState = treeState
                    )
                }
                selectedTabId == "schema" -> {
                    MercuryEmptyState()
                }
                selectedTabId == "diagram" && (uiState is MercuryUiState.Loading || uiState is MercuryUiState.Empty) -> {
                    MercuryEmptyState()
                }
                selectedTabId == "diagram" && database != null -> {
                    DiagramScene(
                        database = database,
                        project = project,
                        diagramState = diagramState
                    )
                }
                selectedTabId == "queries" && (uiState is MercuryUiState.Loading || uiState is MercuryUiState.Empty) -> {
                    MercuryEmptyState()
                }
                selectedTabId == "queries" && database != null -> {
                    QueriesScene(
                        database = database,
                        searchQuery = searchQuery,
                        project = project,
                        queriesState = queriesState
                    )
                }
                selectedTabId == "diff" && schema != null -> {
                    ComparisonScene(
                        schema = schema,
                        project = project,
                        comparisonState = comparisonState
                    )
                }
                else -> {
                    MercuryEmptyState()
                }
            }
        }
    }
}
