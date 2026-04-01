package com.github.merkost.mercury.ui.queries

import com.github.merkost.mercury.model.*
import org.junit.Assert.*
import org.junit.Test

class QueriesSearchTest {

    private val testDaos = listOf(
        DaoInfo(
            name = "UserDao",
            qualifiedName = "com.example.UserDao",
            methods = listOf(
                DaoMethod(name = "getAll", type = DaoMethodType.QUERY, query = "SELECT * FROM users", returnType = "List<User>"),
                DaoMethod(name = "insert", type = DaoMethodType.INSERT, returnType = "Unit")
            )
        ),
        DaoInfo(
            name = "OrderDao",
            qualifiedName = "com.example.OrderDao",
            methods = listOf(
                DaoMethod(name = "findByUser", type = DaoMethodType.QUERY, query = "SELECT * FROM orders WHERE userId = :id", returnType = "List<Order>")
            )
        )
    )

    @Test
    fun emptyQueryReturnsAllDaos() {
        val result = filterDaos(testDaos, "")
        assertEquals(2, result.size)
    }

    @Test
    fun filterByDaoName() {
        val result = filterDaos(testDaos, "UserDao")
        assertEquals(1, result.size)
        assertEquals("UserDao", result[0].name)
    }

    @Test
    fun filterByMethodName() {
        val result = filterDaos(testDaos, "findByUser")
        assertEquals(1, result.size)
        assertEquals("OrderDao", result[0].name)
        assertEquals(1, result[0].methods.size)
    }

    @Test
    fun filterBySqlContent() {
        val result = filterDaos(testDaos, "orders")
        assertEquals(1, result.size)
        assertEquals("OrderDao", result[0].name)
    }

    @Test
    fun filterByReturnType() {
        val result = filterDaos(testDaos, "List<Order>")
        assertEquals(1, result.size)
        assertEquals("OrderDao", result[0].name)
    }

    @Test
    fun noMatchReturnsEmpty() {
        val result = filterDaos(testDaos, "nonexistent")
        assertTrue(result.isEmpty())
    }

    @Test
    fun caseInsensitiveSearch() {
        val result = filterDaos(testDaos, "userdao")
        assertEquals(1, result.size)
    }
}
