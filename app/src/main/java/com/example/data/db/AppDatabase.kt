package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AdminDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.DepositDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserDao
import com.example.data.dao.UserPlanDao
import com.example.data.dao.WithdrawalDao
import com.example.data.model.AdminCredentialsEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.DepositEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserPlanEntity
import com.example.data.model.WithdrawalEntity
import com.example.util.SecurityUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        DepositEntity::class,
        WithdrawalEntity::class,
        TransactionEntity::class,
        UserPlanEntity::class,
        AdminCredentialsEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun depositDao(): DepositDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userPlanDao(): UserPlanDao
    abstract fun adminDao(): AdminDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartearn_central.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial Admin Credentials securely: Initial Admin Password = 451040600
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { appDb ->
                                val defaultPasswordHash = SecurityUtil.hashPassword("451040600")
                                appDb.adminDao().saveAdminCredentials(
                                    AdminCredentialsEntity(
                                        id = 1,
                                        username = "admin",
                                        passwordHash = defaultPasswordHash,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                                appDb.auditLogDao().insertLog(
                                    AuditLogEntity(
                                        userId = "SYSTEM",
                                        action = "SYSTEM_INITIALIZED",
                                        details = "Central Database initialized with secure Admin authentication."
                                    )
                                )
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
