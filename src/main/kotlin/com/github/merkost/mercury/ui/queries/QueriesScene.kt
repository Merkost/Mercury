package com.github.merkost.mercury.ui.queries

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.intellij.openapi.project.Project
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.model.DaoInfo
import com.github.merkost.mercury.ui.components.MercuryCountBadge
import com.github.merkost.mercury.ui.components.onHoverChanged
import com.github.merkost.mercury.ui.components.rememberHoverState
import com.github.merkost.mercury.ui.navigation.PsiNavigationBridge
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun QueriesScene(
    database: DatabaseInfo,
    searchQuery: String,
    project: Project,
    queriesState: QueriesState,
    modifier: Modifier = Modifier
) {
    val filteredDaos = remember(database.daos, searchQuery) {
        filterDaos(database.daos, searchQuery)
    }

    LaunchedEffect(filteredDaos) {
        if (filteredDaos.isNotEmpty()) {
            queriesState.expandFirstDao(filteredDaos.first().qualifiedName)
        }
        val validDaoKeys = filteredDaos.map { it.qualifiedName }.toSet()
        val validMethodKeys = filteredDaos.flatMap { dao ->
            dao.methods.map { "${dao.qualifiedName}:${it.name}" }
        }.toSet()
        queriesState.prune(validDaoKeys, validMethodKeys)
    }

    val entityByTableName = remember(database.entities) {
        database.entities.associateBy { it.tableName }
    }

    if (filteredDaos.isEmpty() && searchQuery.isNotEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No matching queries",
                style = MercuryTheme.typography.bodyMedium,
                color = MercuryTheme.colors.textMuted
            )
        }
        return
    }

    if (filteredDaos.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No DAOs found",
                style = MercuryTheme.typography.bodyMedium,
                color = MercuryTheme.colors.textMuted
            )
        }
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        for (dao in filteredDaos) {
            item(key = "dao:${dao.qualifiedName}") {
                DaoSectionHeader(
                    dao = dao,
                    isExpanded = queriesState.isDaoExpanded(dao.qualifiedName),
                    onToggle = { queriesState.toggleDao(dao.qualifiedName) }
                )
            }

            if (queriesState.isDaoExpanded(dao.qualifiedName)) {
                items(
                    items = dao.methods,
                    key = { "method:${dao.qualifiedName}:${it.name}" }
                ) { method ->
                    val methodKey = "${dao.qualifiedName}:${method.name}"
                    val touchedEntityNames = method.touchedEntities

                    MethodRow(
                        method = method,
                        daoQualifiedName = dao.qualifiedName,
                        isExpanded = queriesState.isMethodExpanded(methodKey),
                        touchedEntityNames = touchedEntityNames,
                        onToggleExpand = { queriesState.toggleMethod(methodKey) },
                        onNavigateToSource = {
                            PsiNavigationBridge.navigateToMethod(project, dao.qualifiedName, method.name)
                        },
                        onNavigateToEntity = { tableName ->
                            val entity = entityByTableName[tableName]
                            if (entity != null) {
                                PsiNavigationBridge.navigateToClass(project, entity.qualifiedName)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DaoSectionHeader(
    dao: DaoInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val colors = MercuryTheme.colors
    val typography = MercuryTheme.typography
    var isHovered by rememberHoverState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isHovered) colors.surfaceHover else Color.Transparent,
        animationSpec = tween(MercuryMotion.durationFast)
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(150, easing = MercuryMotion.easingStandard)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MercurySize.sectionHeaderHeight)
            .background(backgroundColor)
            .onHoverChanged { isHovered = it }
            .clickable(onClick = onToggle)
            .padding(horizontal = MercurySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(MercurySpacing.lg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u25B6",
                style = typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.rotate(chevronRotation)
            )
        }
        Text(
            text = dao.name.uppercase(),
            style = typography.labelSmall.copy(letterSpacing = 0.5.sp),
            color = colors.textMuted
        )
        Spacer(Modifier.weight(1f))
        MercuryCountBadge(dao.methods.size)
        Spacer(Modifier.width(MercurySpacing.sm))
    }
}
