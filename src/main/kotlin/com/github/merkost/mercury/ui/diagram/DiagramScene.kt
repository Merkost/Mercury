package com.github.merkost.mercury.ui.diagram

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.ui.navigation.PsiNavigationBridge
import com.github.merkost.mercury.ui.tree.SchemaTreeNode
import com.github.merkost.mercury.ui.theme.MercuryMotion
import com.github.merkost.mercury.ui.theme.MercurySize
import com.github.merkost.mercury.ui.theme.MercurySpacing
import com.github.merkost.mercury.ui.theme.MercuryTheme

@Composable
fun DiagramScene(
    database: DatabaseInfo,
    project: Project,
    diagramState: DiagramState,
    modifier: Modifier = Modifier
) {
    val colors = MercuryTheme.colors
    val density = LocalDensity.current
    val entitySizes = remember { mutableStateMapOf<String, Pair<Float, Float>>() }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val maxColumns = database.entities.maxOfOrNull { it.columns.size } ?: 5
    val cellWidth = with(density) { (MercurySize.entityCardMaxWidth + 64.dp).toPx() }
    val cellHeight = with(density) { (40.dp + MercurySize.entityFieldHeight * maxColumns + 48.dp).toPx() }

    val edges = remember(database) { buildEdges(database) }

    val animatedZoom by animateFloatAsState(
        targetValue = diagramState.zoom,
        animationSpec = tween(MercuryMotion.durationNormal, easing = MercuryMotion.easingDecelerate)
    )

    LaunchedEffect(database.entities.map { it.qualifiedName }) {
        val validNames = database.entities.map { it.qualifiedName }.toSet()
        diagramState.pruneEntities(validNames)

        val needsLayout = database.entities.any { it.qualifiedName !in diagramState.entityPositions }
        if (needsLayout) {
            val newPositions = DiagramLayoutEngine.computePositions(database.entities, cellWidth, cellHeight)
            for ((name, pos) in newPositions) {
                if (name !in diagramState.entityPositions) {
                    diagramState.setEntityPosition(name, pos)
                }
            }
            if (newPositions.isNotEmpty() && containerSize != IntSize.Zero) {
                val maxX = newPositions.values.maxOf { it.x } + cellWidth
                val maxY = newPositions.values.maxOf { it.y } + cellHeight
                val zoomX = containerSize.width / maxX
                val zoomY = containerSize.height / maxY
                val fitZoom = minOf(zoomX, zoomY, 1f).coerceIn(DiagramState.MIN_ZOOM, 1f)
                diagramState.updateZoom(fitZoom)
            }
        }
    }

    val connectedToSelected = remember(diagramState.selectedEntity, database) {
        if (diagramState.selectedEntity == null) emptySet()
        else {
            edges.filter {
                it.childQualifiedName == diagramState.selectedEntity ||
                    it.parentQualifiedName == diagramState.selectedEntity
            }.flatMap { listOf(it.childQualifiedName, it.parentQualifiedName) }.toSet()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown && event.key == Key.Escape -> {
                        diagramState.deselect()
                        true
                    }
                    event.type == KeyEventType.KeyDown && event.key == Key.Zero &&
                        (event.isMetaPressed || event.isCtrlPressed) -> {
                        diagramState.updateZoom(1f)
                        true
                    }
                    event.type == KeyEventType.KeyDown && event.key == Key.Equals &&
                        (event.isMetaPressed || event.isCtrlPressed) -> {
                        diagramState.zoomIn()
                        true
                    }
                    event.type == KeyEventType.KeyDown && event.key == Key.Minus &&
                        (event.isMetaPressed || event.isCtrlPressed) -> {
                        diagramState.zoomOut()
                        true
                    }
                    else -> false
                }
            }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .background(colors.diagramCanvas)
                .onSizeChanged { containerSize = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { diagramState.deselect() },
                        onDoubleTap = {
                            diagramState.resetPositions()
                            val newPositions = DiagramLayoutEngine.computePositions(database.entities, cellWidth, cellHeight)
                            for ((name, pos) in newPositions) {
                                diagramState.setEntityPosition(name, pos)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if (!diagramState.isDraggingCard) {
                                change.consume()
                                diagramState.updatePan(diagramState.panOffset + dragAmount)
                            }
                        }
                    )
                }
                .pointerInput(diagramState) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val scrollDelta = event.changes.firstOrNull()?.scrollDelta
                            if (scrollDelta != null && scrollDelta.y != 0f) {
                                val zoomDelta = if (scrollDelta.y > 0) -DiagramState.SCROLL_ZOOM_STEP else DiagramState.SCROLL_ZOOM_STEP
                                diagramState.updateZoom(diagramState.zoom + zoomDelta)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = animatedZoom
                        scaleY = animatedZoom
                        translationX = diagramState.panOffset.x
                        translationY = diagramState.panOffset.y
                    }
            ) {
                RelationshipLines(
                    database = database,
                    entityPositions = diagramState.entityPositions,
                    entitySizes = entitySizes,
                    selectedEntity = diagramState.selectedEntity
                )

                for (entity in database.entities) {
                    val position = diagramState.entityPositions[entity.qualifiedName] ?: continue
                    val isDimmed = diagramState.selectedEntity != null &&
                        entity.qualifiedName != diagramState.selectedEntity &&
                        entity.qualifiedName !in connectedToSelected

                    EntityCard(
                        entity = entity,
                        isSelected = entity.qualifiedName == diagramState.selectedEntity,
                        isDimmed = isDimmed,
                        zoom = diagramState.zoom,
                        onSelect = { diagramState.selectEntity(entity.qualifiedName) },
                        onDoubleClick = {
                            PsiNavigationBridge.navigateToNode(
                                project,
                                SchemaTreeNode.Entity(entity)
                            )
                        },
                        onDrag = { dragAmount ->
                            val currentPos = diagramState.entityPositions[entity.qualifiedName] ?: Offset.Zero
                            diagramState.setEntityPosition(
                                entity.qualifiedName,
                                currentPos + dragAmount / diagramState.zoom
                            )
                        },
                        onDragStart = { diagramState.isDraggingCard = true },
                        onDragEnd = { diagramState.isDraggingCard = false },
                        modifier = Modifier
                            .offset(
                                x = with(density) { position.x.toDp() },
                                y = with(density) { position.y.toDp() }
                            )
                            .onSizeChanged { size ->
                                entitySizes[entity.qualifiedName] = Pair(size.width.toFloat(), size.height.toFloat())
                            }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MercurySize.borderWidth)
                .background(colors.borderSubtle)
        )

        DiagramToolbar(
            zoom = diagramState.zoom,
            entityCount = database.entities.size,
            relationCount = edges.size,
            onZoomIn = { diagramState.zoomIn() },
            onZoomOut = { diagramState.zoomOut() },
            onResetZoom = { diagramState.updateZoom(1f) },
            onResetView = { diagramState.resetView() },
            onFitAll = {
                diagramState.resetPositions()
                val newPositions = DiagramLayoutEngine.computePositions(database.entities, cellWidth, cellHeight)
                for ((name, pos) in newPositions) {
                    diagramState.setEntityPosition(name, pos)
                }
            }
        )
    }
}
