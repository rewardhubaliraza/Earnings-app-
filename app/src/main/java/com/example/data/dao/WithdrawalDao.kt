package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WithdrawalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWithdrawalsByUserId(userId: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE status = :status ORDER BY createdAt DESC")
    fun getWithdrawalsByStatus(status: String): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE withdrawalId = :withdrawalId LIMIT 1")
    suspend fun getWithdrawalById(withdrawalId: String): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("UPDATE withdrawals SET status = :status, reviewedAt = :reviewedAt, rejectReason = :reason WHERE withdrawalId = :withdrawalId")
    suspend fun updateWithdrawalStatus(withdrawalId: String, status: String, reviewedAt: Long, reason: String?)

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'Pending'")
    fun getPendingWithdrawalsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'Approved'")
    fun getApprovedWithdrawalsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'Rejected'")
    fun getRejectedWithdrawalsCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM withdrawals WHERE status = 'Approved'")
    fun getTotalApprovedWithdrawalAmount(): Flow<Double?>
}
