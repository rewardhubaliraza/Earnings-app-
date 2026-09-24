package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String? = null,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
