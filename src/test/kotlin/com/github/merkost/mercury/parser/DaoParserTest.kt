package com.github.merkost.mercury.parser

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.merkost.mercury.model.DaoMethodType
import com.github.merkost.mercury.model.OnConflictStrategy

class DaoParserTest : BasePlatformTestCase() {

    private lateinit var daoParser: DaoParser

    override fun setUp() {
        super.setUp()
        daoParser = DaoParser()
    }

    fun testSimpleDao() {
        myFixture.configureByFiles("SimpleDao.kt", "SimpleEntity.kt", "stubs/RoomAnnotations.kt")

        val facade = JavaPsiFacade.getInstance(project)
        val psiClass = facade.findClass("com.example.UserDao", GlobalSearchScope.projectScope(project))
        assertNotNull("UserDao not found", psiClass)

        val dao = daoParser.parse(psiClass!!)
        assertNotNull("DAO parse returned null", dao)
        dao!!

        assertEquals("UserDao", dao.name)
        assertEquals(4, dao.methods.size)

        val getAll = dao.methods.find { it.name == "getAll" }
        assertNotNull(getAll)
        assertEquals(DaoMethodType.QUERY, getAll!!.type)
        assertEquals("SELECT * FROM users", getAll.query)
        assertTrue(getAll.touchedEntities.contains("users"))

        val insert = dao.methods.find { it.name == "insert" }
        assertNotNull(insert)
        assertEquals(DaoMethodType.INSERT, insert!!.type)
        assertEquals(OnConflictStrategy.REPLACE, insert.onConflict)
        assertEquals(1, insert.parameters.size)

        val delete = dao.methods.find { it.name == "delete" }
        assertNotNull(delete)
        assertEquals(DaoMethodType.DELETE, delete!!.type)
    }

    override fun getTestDataPath() = "src/test/testData"
}
