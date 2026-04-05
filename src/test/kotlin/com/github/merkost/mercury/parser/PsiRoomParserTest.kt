package com.github.merkost.mercury.parser

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PsiRoomParserTest : BasePlatformTestCase() {

    fun testParseProjectWithRoomEntities() {
        myFixture.configureByFiles(
            "SimpleDatabase.kt",
            "SimpleEntity.kt",
            "EntityWithRelations.kt",
            "SimpleDao.kt",
            "stubs/RoomAnnotations.kt"
        )

        val parser = PsiRoomParser(project)
        val schema = parser.parseProject()

        assertFalse("Schema should have databases", schema.databases.isEmpty())

        val db = schema.databases.first()
        assertEquals("AppDatabase", db.name)
        assertEquals(3, db.version)
        assertTrue("Should have entities", db.entities.isNotEmpty())
        assertTrue("Should have DAOs", db.daos.isNotEmpty())
    }

    override fun getTestDataPath() = "src/test/testData"
}
