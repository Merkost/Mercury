package com.github.merkost.mercury.ui.tree

import org.junit.Assert.*
import org.junit.Test

class SchemaTreeStateTest {

    @Test
    fun pruneRemovesExpandedKeysThatNoLongerExist() {
        val state = SchemaTreeState(
            initialExpanded = setOf("db:com.example.AppDb", "entity:com.example.User", "entity:com.example.Deleted")
        )
        val validKeys = setOf("db:com.example.AppDb", "entity:com.example.User", "section:com.example.AppDb:ENTITIES")

        state.pruneStaleKeys(validKeys)

        assertTrue(state.isExpanded("db:com.example.AppDb"))
        assertTrue(state.isExpanded("entity:com.example.User"))
        assertFalse(state.isExpanded("entity:com.example.Deleted"))
    }

    @Test
    fun pruneClearsSelectionIfKeyNoLongerExists() {
        val state = SchemaTreeState()
        state.select("entity:com.example.Deleted")
        assertEquals("entity:com.example.Deleted", state.selectedKey)

        state.pruneStaleKeys(setOf("db:com.example.AppDb"))

        assertNull(state.selectedKey)
    }

    @Test
    fun pruneKeepsSelectionIfKeyStillValid() {
        val state = SchemaTreeState()
        state.select("entity:com.example.User")

        state.pruneStaleKeys(setOf("entity:com.example.User", "db:com.example.AppDb"))

        assertEquals("entity:com.example.User", state.selectedKey)
    }

    @Test
    fun pruneHandlesEmptyValidKeys() {
        val state = SchemaTreeState(initialExpanded = setOf("db:com.example.AppDb"))
        state.select("entity:com.example.User")

        state.pruneStaleKeys(emptySet())

        assertTrue(state.expandedKeys.isEmpty())
        assertNull(state.selectedKey)
    }
}
