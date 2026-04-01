package com.github.merkost.mercury.ui.diagram

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.*
import org.junit.Test

class DiagramStateTest {

    @Test
    fun initialStateHasDefaultZoomAndNoPan() {
        val state = DiagramState()
        assertEquals(1f, state.zoom, 0.001f)
        assertEquals(Offset.Zero, state.panOffset)
        assertNull(state.selectedEntity)
    }

    @Test
    fun zoomClampsToMinimum() {
        val state = DiagramState()
        state.updateZoom(0.1f)
        assertEquals(DiagramState.MIN_ZOOM, state.zoom, 0.001f)
    }

    @Test
    fun zoomClampsToMaximum() {
        val state = DiagramState()
        state.updateZoom(5f)
        assertEquals(DiagramState.MAX_ZOOM, state.zoom, 0.001f)
    }

    @Test
    fun setEntityPositionStoresPosition() {
        val state = DiagramState()
        state.setEntityPosition("com.example.User", Offset(100f, 200f))
        assertEquals(Offset(100f, 200f), state.entityPositions["com.example.User"])
    }

    @Test
    fun selectEntityUpdatesSelection() {
        val state = DiagramState()
        state.selectEntity("com.example.User")
        assertEquals("com.example.User", state.selectedEntity)
    }

    @Test
    fun deselectClearsSelection() {
        val state = DiagramState()
        state.selectEntity("com.example.User")
        state.deselect()
        assertNull(state.selectedEntity)
    }

    @Test
    fun pruneRemovesPositionsForDeletedEntities() {
        val state = DiagramState()
        state.setEntityPosition("com.example.User", Offset(100f, 200f))
        state.setEntityPosition("com.example.Deleted", Offset(300f, 400f))
        state.pruneEntities(setOf("com.example.User"))
        assertNotNull(state.entityPositions["com.example.User"])
        assertNull(state.entityPositions["com.example.Deleted"])
    }

    @Test
    fun pruneDeselectsRemovedEntity() {
        val state = DiagramState()
        state.selectEntity("com.example.Deleted")
        state.pruneEntities(setOf("com.example.User"))
        assertNull(state.selectedEntity)
    }

    @Test
    fun zoomInIncrementsByStep() {
        val state = DiagramState()
        state.zoomIn()
        assertEquals(1.25f, state.zoom, 0.001f)
    }

    @Test
    fun zoomOutDecrementsByStep() {
        val state = DiagramState()
        state.zoomOut()
        assertEquals(0.75f, state.zoom, 0.001f)
    }
}
