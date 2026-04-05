package com.github.merkost.mercury.ui.comparison

import org.junit.Assert.*
import org.junit.Test

class ComparisonStateTest {
    @Test fun autoSelectsLastTwoVersions() {
        val state = ComparisonState()
        state.autoSelect(listOf("AppDb", "TestDb", "OtherDb"))
        assertEquals("TestDb", state.leftVersion)
        assertEquals("OtherDb", state.rightVersion)
    }
    @Test fun autoSelectWithOneDatabaseSelectsSameForBoth() {
        val state = ComparisonState()
        state.autoSelect(listOf("AppDb"))
        assertEquals("AppDb", state.leftVersion)
        assertEquals("AppDb", state.rightVersion)
    }
    @Test fun swapExchangesDatabases() {
        val state = ComparisonState()
        state.autoSelect(listOf("AppDb", "TestDb"))
        state.swap()
        assertEquals("TestDb", state.leftVersion)
        assertEquals("AppDb", state.rightVersion)
    }
    @Test fun toggleEntityExpandsAndCollapses() {
        val state = ComparisonState()
        state.toggleEntity("User")
        assertTrue(state.isEntityExpanded("User"))
        state.toggleEntity("User")
        assertFalse(state.isEntityExpanded("User"))
    }
    @Test fun autoSelectDoesNothingIfAlreadySelected() {
        val state = ComparisonState()
        state.selectLeft("Custom")
        state.selectRight("Other")
        state.autoSelect(listOf("AppDb", "TestDb"))
        assertEquals("Custom", state.leftVersion)
        assertEquals("Other", state.rightVersion)
    }
}
