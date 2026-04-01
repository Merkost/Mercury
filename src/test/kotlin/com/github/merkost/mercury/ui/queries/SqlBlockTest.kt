package com.github.merkost.mercury.ui.queries

import org.junit.Assert.*
import org.junit.Test

class SqlBlockTest {

    @Test
    fun detectsSelectKeyword() {
        val segments = parseSqlSegments("SELECT * FROM users")
        assertEquals(4, segments.size)
        assertTrue(segments[0].isKeyword)
        assertEquals("SELECT", segments[0].text)
        assertFalse(segments[1].isKeyword)
        assertEquals(" * ", segments[1].text)
        assertTrue(segments[2].isKeyword)
        assertEquals("FROM", segments[2].text)
    }

    @Test
    fun handlesLowerCaseKeywords() {
        val segments = parseSqlSegments("select * from users")
        assertTrue(segments[0].isKeyword)
        assertEquals("select", segments[0].text)
    }

    @Test
    fun handlesMixedContent() {
        val segments = parseSqlSegments("SELECT name, age FROM users WHERE id = :userId")
        val keywords = segments.filter { it.isKeyword }.map { it.text.uppercase() }
        assertTrue(keywords.contains("SELECT"))
        assertTrue(keywords.contains("FROM"))
        assertTrue(keywords.contains("WHERE"))
    }

    @Test
    fun emptyQueryReturnsEmptyList() {
        val segments = parseSqlSegments("")
        assertTrue(segments.isEmpty())
    }

    @Test
    fun noKeywordsReturnsNonKeywordSegment() {
        val segments = parseSqlSegments("users")
        assertEquals(1, segments.size)
        assertFalse(segments[0].isKeyword)
    }

    @Test
    fun handlesJoinKeywords() {
        val segments = parseSqlSegments("SELECT * FROM users LEFT JOIN orders ON users.id = orders.userId")
        val keywords = segments.filter { it.isKeyword }.map { it.text.uppercase() }
        assertTrue(keywords.contains("LEFT"))
        assertTrue(keywords.contains("JOIN"))
        assertTrue(keywords.contains("ON"))
    }
}
