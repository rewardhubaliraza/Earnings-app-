package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deposits",
    indices = [
        Index(value = ["depositId"], unique = true),
        Index(value = ["userId"])
    ]
)
data class DepositEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val depositId: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val planId: Int,
    val planName: String,
    val amount: Double,
    val paymentMethod: String = "JazzCash",
    val recipientAccount: String = "03227422095",
    val senderPhone: String,
    val transactionReference: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val rejectReason: String? = null
)
