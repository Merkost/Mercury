package com.example

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["timestamp"], unique = true)
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val user_id: Long,
    val content: String,
    val timestamp: Long
)
