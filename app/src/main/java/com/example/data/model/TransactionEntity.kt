package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["transactionId"], unique = true),
        Index(value = ["userId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val transactionId: String,
    val userId: String,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "DAILY_RETURN", "ADMIN_ADJUST", "PLAN_ACTIVATED"
    val amount: Double,
    val status: String = "Completed",
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
