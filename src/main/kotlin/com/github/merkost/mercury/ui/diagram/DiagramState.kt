package com.github.merkost.mercury.ui.diagram

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

@Stable
class DiagramState {

    var entityPositions = mutableStateMapOf<String, Offset>()
        private set

    var zoom by mutableFloatStateOf(1f)
        private set

    var panOffset by mutableStateOf(Offset.Zero)
        private set

    var selectedEntity: String? by mutableStateOf(null)
        private set

    var isDraggingCard by mutableStateOf(false)

    fun setEntityPosition(qualifiedName: String, position: Offset) {
        entityPositions[qualifiedName] = position
    }

    fun updateZoom(newZoom: Float) {
        zoom = newZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    fun zoomIn() {
        updateZoom(zoom + ZOOM_STEP)
    }

    fun zoomOut() {
        updateZoom(zoom - ZOOM_STEP)
    }

    fun updatePan(newOffset: Offset) {
        panOffset = newOffset
    }

    fun selectEntity(qualifiedName: String) {
        selectedEntity = qualifiedName
    }

    fun deselect() {
        selectedEntity = null
    }

    fun pruneEntities(validQualifiedNames: Set<String>) {
        val toRemove = entityPositions.keys - validQualifiedNames
        toRemove.forEach { entityPositions.remove(it) }
        if (selectedEntity != null && selectedEntity !in validQualifiedNames) {
            selectedEntity = null
        }
    }

    fun resetPositions() {
        entityPositions.clear()
        panOffset = Offset.Zero
        zoom = 1f
        selectedEntity = null
    }

    fun resetView() {
        panOffset = Offset.Zero
        zoom = 1f
    }

    companion object {
        const val MIN_ZOOM = 0.25f
        const val MAX_ZOOM = 2f
        const val ZOOM_STEP = 0.25f
        const val SCROLL_ZOOM_STEP = 0.05f
    }
}

@Composable
fun rememberDiagramState(databaseQualifiedName: String): DiagramState {
    return remember(databaseQualifiedName) { DiagramState() }
}
