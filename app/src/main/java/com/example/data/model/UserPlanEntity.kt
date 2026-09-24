package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_plans",
    indices = [
        Index(value = ["userId"])
    ]
)
data class UserPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val planId: Int,
    val planName: String,
    val amount: Double,
    val dailyReturn: Double,
    val status: String = "Active", // "Active", "Completed", "Cancelled"
    val depositReference: String? = null,
    val activatedAt: Long = System.currentTimeMillis()
)
