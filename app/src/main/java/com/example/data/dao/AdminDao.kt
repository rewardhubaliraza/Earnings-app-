package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AdminCredentialsEntity

@Dao
interface AdminDao {
    @Query("SELECT * FROM admin_credentials WHERE id = 1 LIMIT 1")
    suspend fun getAdminCredentials(): AdminCredentialsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminCredentials(credentials: AdminCredentialsEntity)

    @Query("UPDATE admin_credentials SET passwordHash = :passwordHash, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updatePassword(passwordHash: String, updatedAt: Long)
}
