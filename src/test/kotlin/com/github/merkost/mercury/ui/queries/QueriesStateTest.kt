package com.github.merkost.mercury.ui.queries

import org.junit.Assert.*
import org.junit.Test

class QueriesStateTest {

    @Test
    fun initialStateHasNoExpansions() {
        val state = QueriesState()
        assertTrue(state.expandedDaos.isEmpty())
        assertTrue(state.expandedMethods.isEmpty())
    }

    @Test
    fun toggleDaoExpandsAndCollapses() {
        val state = QueriesState()
        state.toggleDao("com.example.UserDao")
        assertTrue(state.isDaoExpanded("com.example.UserDao"))
        state.toggleDao("com.example.UserDao")
        assertFalse(state.isDaoExpanded("com.example.UserDao"))
    }

    @Test
    fun toggleMethodExpandsAndCollapses() {
        val state = QueriesState()
        state.toggleMethod("com.example.UserDao:getAll")
        assertTrue(state.isMethodExpanded("com.example.UserDao:getAll"))
        state.toggleMethod("com.example.UserDao:getAll")
        assertFalse(state.isMethodExpanded("com.example.UserDao:getAll"))
    }

    @Test
    fun expandFirstDaoSetsInitialExpansion() {
        val state = QueriesState()
        state.expandFirstDao("com.example.UserDao")
        assertTrue(state.isDaoExpanded("com.example.UserDao"))
    }

    @Test
    fun expandFirstDaoDoesNothingIfAlreadyHasExpansions() {
        val state = QueriesState()
        state.toggleDao("com.example.ServiceDao")
        state.expandFirstDao("com.example.UserDao")
        assertFalse(state.isDaoExpanded("com.example.UserDao"))
        assertTrue(state.isDaoExpanded("com.example.ServiceDao"))
    }

    @Test
    fun pruneRemovesInvalidDaosAndMethods() {
        val state = QueriesState()
        state.toggleDao("com.example.UserDao")
        state.toggleDao("com.example.DeletedDao")
        state.toggleMethod("com.example.UserDao:getAll")
        state.toggleMethod("com.example.DeletedDao:find")
        state.prune(setOf("com.example.UserDao"), setOf("com.example.UserDao:getAll"))
        assertTrue(state.isDaoExpanded("com.example.UserDao"))
        assertFalse(state.isDaoExpanded("com.example.DeletedDao"))
        assertTrue(state.isMethodExpanded("com.example.UserDao:getAll"))
        assertFalse(state.isMethodExpanded("com.example.DeletedDao:find"))
    }
}
