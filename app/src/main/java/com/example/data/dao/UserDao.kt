package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY registeredAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserByUserId(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByUserIdOnce(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET balance = :newBalance WHERE userId = :userId")
    suspend fun updateBalance(userId: String, newBalance: Double)

    @Query("UPDATE users SET status = :status WHERE userId = :userId")
    suspend fun updateStatus(userId: String, status: String)

    @Query("UPDATE users SET activePlanId = :planId, activePlanName = :planName, dailyReturn = :dailyReturn WHERE userId = :userId")
    suspend fun updateActivePlan(userId: String, planId: Int?, planName: String?, dailyReturn: Double)

    @Query("UPDATE users SET balance = :newBalance, lastEarningDate = :date WHERE userId = :userId")
    suspend fun updateDailyEarning(userId: String, date: String, newBalance: Double)

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUserCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE activePlanId IS NULL")
    fun getUsersWithoutPlanCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE activePlanId IS NOT NULL")
    fun getUsersWithActivePlanCount(): Flow<Int>
}
