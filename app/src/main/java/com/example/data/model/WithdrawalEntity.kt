package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "withdrawals",
    indices = [
        Index(value = ["withdrawalId"], unique = true),
        Index(value = ["userId"])
    ]
)
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val withdrawalId: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val amount: Double,
    val paymentMethod: String = "JazzCash",
    val accountTitle: String,
    val accountNumber: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val rejectReason: String? = null
)
