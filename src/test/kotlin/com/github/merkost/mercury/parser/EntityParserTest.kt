package com.github.merkost.mercury.parser

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.merkost.mercury.kmp.SourceSetScanner
import com.github.merkost.mercury.model.ForeignKeyAction

class EntityParserTest : BasePlatformTestCase() {

    private lateinit var entityParser: EntityParser

    override fun setUp() {
        super.setUp()
        entityParser = EntityParser(SourceSetScanner(project))
    }

    fun testSimpleEntity() {
        myFixture.configureByFiles("SimpleEntity.kt", "stubs/RoomAnnotations.kt")

        val facade = JavaPsiFacade.getInstance(project)
        val psiClass = facade.findClass("com.example.UserEntity", GlobalSearchScope.projectScope(project))
        assertNotNull("UserEntity class not found", psiClass)

        val entity = entityParser.parse(psiClass!!)
        assertNotNull("Entity parse returned null", entity)
        entity!!

        assertEquals("UserEntity", entity.name)
        assertEquals("users", entity.tableName)
        assertTrue("Expected columns but got ${entity.columns.size}", entity.columns.isNotEmpty())
        assertTrue("Expected PK to contain 'id' but got ${entity.primaryKey.columnNames}",
            entity.primaryKey.columnNames.contains("id"))
        assertTrue("Expected autoGenerate=true", entity.primaryKey.autoGenerate)

        val nameCol = entity.columns.find { it.name == "name" }
        assertNotNull(nameCol)
        assertEquals("user_name", nameCol!!.columnName)

        val ageCol = entity.columns.find { it.name == "age" }
        assertNotNull(ageCol)
        assertEquals("0", ageCol!!.defaultValue)
    }

    fun testEntityWithForeignKeys() {
        myFixture.configureByFiles("EntityWithRelations.kt", "SimpleEntity.kt", "stubs/RoomAnnotations.kt")

        val facade = JavaPsiFacade.getInstance(project)
        val psiClass = facade.findClass("com.example.MessageEntity", GlobalSearchScope.projectScope(project))
        assertNotNull(psiClass)

        val entity = entityParser.parse(psiClass!!)
        assertNotNull(entity)
        entity!!

        assertEquals("messages", entity.tableName)
        assertEquals(1, entity.foreignKeys.size)

        val fk = entity.foreignKeys.first()
        assertEquals("UserEntity", fk.parentEntity)
        assertEquals(listOf("id"), fk.parentColumns)
        assertEquals(listOf("user_id"), fk.childColumns)
        assertEquals(ForeignKeyAction.CASCADE, fk.onDelete)

        assertEquals(2, entity.indices.size)
        assertTrue(entity.indices.any { it.isUnique && it.columnNames == listOf("timestamp") })
    }

    override fun getTestDataPath() = "src/test/testData"
}
