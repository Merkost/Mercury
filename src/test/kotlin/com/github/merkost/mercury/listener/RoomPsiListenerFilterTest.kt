package com.github.merkost.mercury.listener

import org.junit.Assert.*
import org.junit.Test

class RoomPsiListenerFilterTest {

    @Test
    fun detectsFileWithRoomImport() {
        val text = """
            package com.example
            import androidx.room.Entity

            @Entity(tableName = "users")
            data class User(val id: Int, val name: String)
        """.trimIndent()

        assertTrue(RoomPsiListener.isRoomRelatedFile(text))
    }

    @Test
    fun detectsFileWithDatabaseAnnotation() {
        val text = """
            package com.example

            @Database(entities = [User::class], version = 1)
            abstract class AppDatabase : RoomDatabase()
        """.trimIndent()

        assertTrue(RoomPsiListener.isRoomRelatedFile(text))
    }

    @Test
    fun detectsFileWithDaoAnnotation() {
        val text = """
            package com.example

            @Dao
            interface UserDao {
                fun getAll(): List<User>
            }
        """.trimIndent()

        assertTrue(RoomPsiListener.isRoomRelatedFile(text))
    }

    @Test
    fun ignoresUnrelatedKotlinFile() {
        val text = """
            package com.example
            import kotlinx.coroutines.flow.Flow

            class UserRepository(private val api: ApiService) {
                fun getUsers(): Flow<List<User>> = api.fetchUsers()
            }
        """.trimIndent()

        assertFalse(RoomPsiListener.isRoomRelatedFile(text))
    }

    @Test
    fun ignoresEmptyFile() {
        assertFalse(RoomPsiListener.isRoomRelatedFile(""))
    }

    @Test
    fun detectsFileWithEntityAnnotationNoImport() {
        val text = """
            package com.example

            @androidx.room.Entity(tableName = "users")
            data class User(val id: Int, val name: String)
        """.trimIndent()

        assertTrue(RoomPsiListener.isRoomRelatedFile(text))
    }
}
