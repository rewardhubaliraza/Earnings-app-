package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DepositEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits ORDER BY createdAt DESC")
    fun getAllDeposits(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDepositsByUserId(userId: String): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE status = :status ORDER BY createdAt DESC")
    fun getDepositsByStatus(status: String): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE depositId = :depositId LIMIT 1")
    suspend fun getDepositByDepositId(depositId: String): DepositEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)

    @Query("UPDATE deposits SET status = :status, reviewedAt = :reviewedAt, rejectReason = :reason WHERE depositId = :depositId")
    suspend fun updateDepositStatus(depositId: String, status: String, reviewedAt: Long, reason: String?)

    @Query("SELECT COUNT(*) FROM deposits WHERE status = 'Pending'")
    fun getPendingDepositsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM deposits WHERE status = 'Approved'")
    fun getApprovedDepositsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM deposits WHERE status = 'Rejected'")
    fun getRejectedDepositsCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM deposits WHERE status = 'Approved'")
    fun getTotalApprovedDepositAmount(): Flow<Double?>
}
