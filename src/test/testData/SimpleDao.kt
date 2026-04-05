package com.example

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Delete
    fun delete(user: UserEntity)
}
