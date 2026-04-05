package com.github.merkost.mercury.ui.comparison

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.github.merkost.mercury.analysis.SchemaDiffer
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.model.MatchType
import com.github.merkost.mercury.model.RoomSchema
import com.github.merkost.mercury.parser.SchemaDiscovery
import com.github.merkost.mercury.parser.SchemaVersion
import com.github.merkost.mercury.ui.components.MercuryDropdown
import com.github.merkost.mercury.ui.components.onHoverChanged
import com.github.merkost.mercury.ui.components.rememberHoverState
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

private const val CURRENT_LABEL = "Current"

@Composable
fun ComparisonScene(
    schema: RoomSchema,
    project: Project,
    comparisonState: ComparisonState,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors

    val schemaVersions = remember(project) {
        ReadAction.compute<List<SchemaVersion>, Throwable> {
            SchemaDiscovery.findSchemaVersions(project)
        }
    }

    val currentDb = schema.databases.firstOrNull()
    val relevantVersions = schemaVersions.filter { sv ->
        currentDb == null || sv.databaseQualifiedName.contains(currentDb.name, ignoreCase = true) ||
            currentDb.qualifiedName.contains(sv.databaseQualifiedName, ignoreCase = true) ||
            sv.databaseQualifiedName == currentDb.qualifiedName
    }

    val versionLabels = remember(relevantVersions, currentDb) {
        val labels = relevantVersions.map { "v${it.version}" }.toMutableList()
        if (currentDb != null) labels.add(CURRENT_LABEL)
        labels
    }

    LaunchedEffect(versionLabels) {
        comparisonState.autoSelect(versionLabels)
    }

    if (versionLabels.size < 2) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "\u2205",
                style = MercuryTheme.typography.titleLarge,
                color = colors.textMuted
            )
            Spacer(Modifier.height(MercurySpacing.sm))
            Text(
                text = "No exported schemas found",
                style = MercuryTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(MercurySpacing.xs))
            Text(
                text = "Enable room.schemaLocation in build.gradle",
                style = MercuryTheme.typography.labelMedium,
                color = colors.textMuted
            )
        }
        return
    }

    val leftDb = remember(comparisonState.leftVersion, relevantVersions, currentDb) {
        resolveVersion(comparisonState.leftVersion, relevantVersions, currentDb)
    }
    val rightDb = remember(comparisonState.rightVersion, relevantVersions, currentDb) {
        resolveVersion(comparisonState.rightVersion, relevantVersions, currentDb)
    }

    val diff = remember(leftDb, rightDb) {
        if (leftDb != null && rightDb != null) SchemaDiffer.compare(leftDb, rightDb) else null
    }

    Column(modifier = modifier.fillMaxSize()) {
        VersionSelector(
            versionLabels = versionLabels,
            leftVersion = comparisonState.leftVersion,
            rightVersion = comparisonState.rightVersion,
            onLeftSelected = { comparisonState.selectLeft(it) },
            onRightSelected = { comparisonState.selectRight(it) },
            onSwap = { comparisonState.swap() }
        )

        if (diff != null) {
            MigrationSummary(
                leftLabel = comparisonState.leftVersion,
                rightLabel = comparisonState.rightVersion,
                diff = diff
            )

            val changedEntities = diff.matchedEntities.filter { it.matchType != MatchType.IDENTICAL }
            val identicalCount = diff.matchedEntities.size - changedEntities.size

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (changedEntities.isNotEmpty()) {
                    item(key = "header:modified") {
                        SectionLabel("MODIFIED ENTITIES", changedEntities.size)
                    }
                }
                items(changedEntities, key = { "match:${it.left.name}" }) { match ->
                    EntityDiffRow(
                        leftEntity = match.left,
                        rightEntity = match.right,
                        matchType = match.matchType,
                        onClick = { comparisonState.toggleEntity(match.left.name) }
                    )
                    ColumnDiffBlock(
                        columnDiffs = match.columnDiffs,
                        isExpanded = comparisonState.isEntityExpanded(match.left.name)
                    )
                }
                if (diff.rightOnly.isNotEmpty()) {
                    item(key = "header:added") {
                        SectionLabel("ADDED ENTITIES", diff.rightOnly.size)
                    }
                }
                items(diff.rightOnly, key = { "right:${it.name}" }) { entity ->
                    EntityDiffRow(leftEntity = null, rightEntity = entity, matchType = null, onClick = {})
                }

                if (diff.leftOnly.isNotEmpty()) {
                    item(key = "header:removed") {
                        SectionLabel("REMOVED ENTITIES", diff.leftOnly.size)
                    }
                }
                items(diff.leftOnly, key = { "left:${it.name}" }) { entity ->
                    EntityDiffRow(leftEntity = entity, rightEntity = null, matchType = null, onClick = {})
                }

                if (identicalCount > 0) {
                    item(key = "header:identical") {
                        SectionLabel("UNCHANGED ENTITIES", identicalCount)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(MercurySize.borderWidth).background(colors.borderSubtle))
            ComparisonSummary(diff = diff)
        }
    }
}

@Composable
private fun SectionLabel(title: String, count: Int) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MercurySpacing.lg, vertical = MercurySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = colors.textMuted
        )
        Spacer(Modifier.width(MercurySpacing.sm))
        Text(
            text = count.toString(),
            style = typography.labelSmall,
            color = colors.textMuted,
            modifier = Modifier
                .background(colors.surfaceRaised, androidx.compose.foundation.shape.RoundedCornerShape(MercurySize.radiusSm))
                .padding(horizontal = MercurySpacing.xs, vertical = MercurySpacing.xxs)
        )
    }
}

private fun resolveVersion(
    label: String,
    versions: List<SchemaVersion>,
    currentDb: DatabaseInfo?
): DatabaseInfo? {
    if (label == CURRENT_LABEL) return currentDb
    val versionNumber = label.removePrefix("v").toIntOrNull() ?: return null
    val sv = versions.find { it.version == versionNumber } ?: return null
    return SchemaDiscovery.loadVersion(sv)
}

@Composable
private fun VersionSelector(
    versionLabels: List<String>,
    leftVersion: String,
    rightVersion: String,
    onLeftSelected: (String) -> Unit,
    onRightSelected: (String) -> Unit,
    onSwap: () -> Unit
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var swapHovered by rememberHoverState()

    val swapColor by animateColorAsState(
        targetValue = if (swapHovered) colors.textSecondary else colors.textMuted,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MercurySpacing.lg, vertical = MercurySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        MercuryDropdown(
            selectedValue = leftVersion,
            options = versionLabels,
            onOptionSelected = onLeftSelected
        )
        Text(
            text = " \u21C4 ",
            style = typography.titleSmall,
            color = swapColor,
            modifier = Modifier
                .onHoverChanged { swapHovered = it }
                .clickable(onClick = onSwap)
                .padding(horizontal = MercurySpacing.sm)
        )
        MercuryDropdown(
            selectedValue = rightVersion,
            options = versionLabels,
            onOptionSelected = onRightSelected
        )
    }
}
