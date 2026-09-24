package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_credentials")
data class AdminCredentialsEntity(
    @PrimaryKey
    val id: Int = 1,
    val username: String = "admin",
    val passwordHash: String,
    val updatedAt: Long = System.currentTimeMillis()
)
