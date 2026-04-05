package com.example

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, MessageEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
