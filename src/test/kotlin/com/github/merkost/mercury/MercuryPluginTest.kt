package com.github.merkost.mercury

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.merkost.mercury.services.MercuryProjectService
import com.github.merkost.mercury.model.DatabaseInfo
import com.github.merkost.mercury.model.RoomSchema

class MercuryPluginTest : BasePlatformTestCase() {

    fun testProjectServiceLoads() {
        val service = project.service<MercuryProjectService>()
        assertNotNull(service)
    }

    fun testInitialSchemaIsEmpty() {
        val service = project.service<MercuryProjectService>()
        assertTrue(service.schema.databases.isEmpty())
    }

    fun testSchemaUpdate() {
        val service = project.service<MercuryProjectService>()
        val newSchema = RoomSchema(
            databases = listOf(
                DatabaseInfo(
                    name = "TestDb",
                    qualifiedName = "com.test.TestDb",
                    version = 1,
                    entities = emptyList(),
                    views = emptyList(),
                    typeConverters = emptyList(),
                    daos = emptyList()
                )
            )
        )
        service.updateSchema(newSchema)
        assertEquals(1, service.schema.databases.size)
        assertEquals("TestDb", service.schema.databases.first().name)
    }
}
