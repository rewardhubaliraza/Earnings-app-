package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPlanDao {
    @Query("SELECT * FROM user_plans ORDER BY activatedAt DESC")
    fun getAllUserPlans(): Flow<List<UserPlanEntity>>

    @Query("SELECT * FROM user_plans WHERE userId = :userId ORDER BY activatedAt DESC")
    fun getUserPlansByUserId(userId: String): Flow<List<UserPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlan(userPlan: UserPlanEntity): Long

    @Query("SELECT COUNT(*) FROM user_plans WHERE status = 'Active'")
    fun getActivePlansCount(): Flow<Int>
}
