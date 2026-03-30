package com.github.merkost.mercury.model

import org.junit.Assert.*
import org.junit.Test

class DaoInfoTest {

    @Test
    fun `dao with query method`() {
        val method = DaoMethod(
            name = "getAll",
            type = DaoMethodType.QUERY,
            query = "SELECT * FROM users",
            returnType = "Flow<List<UserEntity>>",
            parameters = emptyList(),
            touchedEntities = listOf("users")
        )

        assertEquals(DaoMethodType.QUERY, method.type)
        assertEquals("SELECT * FROM users", method.query)
        assertEquals(1, method.touchedEntities.size)
    }

    @Test
    fun `dao with insert method and conflict strategy`() {
        val method = DaoMethod(
            name = "insert",
            type = DaoMethodType.INSERT,
            returnType = "Unit",
            parameters = listOf(DaoParameter(name = "user", type = "UserEntity")),
            onConflict = OnConflictStrategy.REPLACE
        )

        assertEquals(DaoMethodType.INSERT, method.type)
        assertEquals(OnConflictStrategy.REPLACE, method.onConflict)
        assertEquals(1, method.parameters.size)
    }

    @Test
    fun `dao with parameterized query`() {
        val method = DaoMethod(
            name = "getById",
            type = DaoMethodType.QUERY,
            query = "SELECT * FROM users WHERE id = :id",
            returnType = "UserEntity?",
            parameters = listOf(DaoParameter(name = "id", type = "Long"))
        )

        assertEquals(1, method.parameters.size)
        assertEquals("id", method.parameters.first().name)
    }

    @Test
    fun `dao info groups methods`() {
        val dao = DaoInfo(
            name = "UserDao",
            qualifiedName = "com.example.UserDao",
            methods = listOf(
                DaoMethod(name = "getAll", type = DaoMethodType.QUERY, query = "SELECT * FROM users", returnType = "List<UserEntity>"),
                DaoMethod(name = "insert", type = DaoMethodType.INSERT, returnType = "Unit"),
                DaoMethod(name = "delete", type = DaoMethodType.DELETE, returnType = "Unit")
            )
        )

        assertEquals(3, dao.methods.size)
        assertEquals(1, dao.methods.count { it.type == DaoMethodType.QUERY })
    }
}
