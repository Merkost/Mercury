package com.example

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_name")
    val name: String,
    val email: String?,
    @ColumnInfo(defaultValue = "0")
    val age: Int = 0
)
