package com.github.merkost.mercury.ui.tree

import org.junit.Assert.*
import org.junit.Test

class SchemaTreeSearchTest {

    @Test
    fun `fuzzyMatch matches substring order`() {
        assertTrue(fuzzyMatch("ue", "UserEntity"))
        assertTrue(fuzzyMatch("user", "UserEntity"))
        assertTrue(fuzzyMatch("UE", "UserEntity"))
        assertFalse(fuzzyMatch("xyz", "UserEntity"))
        assertFalse(fuzzyMatch("eu", "UserEntity"))
    }

    @Test
    fun `fuzzyMatch handles empty query`() {
        assertTrue(fuzzyMatch("", "anything"))
    }

    @Test
    fun `fuzzyMatch is case insensitive`() {
        assertTrue(fuzzyMatch("user", "USERENTITY"))
        assertTrue(fuzzyMatch("USER", "userentity"))
    }
}
