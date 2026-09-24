package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["userId"], unique = true),
        Index(value = ["phone"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val passwordHash: String,
    val balance: Double = 0.0,
    val status: String = "Registered", // "Registered", "Active", "Suspended"
    val activePlanId: Int? = null,     // MUST be null on registration
    val activePlanName: String? = null,// MUST be null on registration
    val dailyReturn: Double = 0.0,     // MUST be 0.0 on registration
    val lastEarningDate: String? = null,
    val registeredAt: Long = System.currentTimeMillis()
)
