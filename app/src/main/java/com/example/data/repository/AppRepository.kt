package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AdminCredentialsEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.DepositEntity
import com.example.data.model.InvestmentPlan
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserPlanEntity
import com.example.data.model.WithdrawalEntity
import com.example.util.DateUtil
import com.example.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AppRepository(private val db: AppDatabase) {

    // DAOs
    val userDao = db.userDao()
    val depositDao = db.depositDao()
    val withdrawalDao = db.withdrawalDao()
    val transactionDao = db.transactionDao()
    val userPlanDao = db.userPlanDao()
    val adminDao = db.adminDao()
    val auditLogDao = db.auditLogDao()

    // Real DB Flows
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allDeposits: Flow<List<DepositEntity>> = depositDao.getAllDeposits()
    val allWithdrawals: Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawals()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()

    // Statistics flows
    val totalUsersCount: Flow<Int> = userDao.getTotalUserCount()
    val usersWithoutPlanCount: Flow<Int> = userDao.getUsersWithoutPlanCount()
    val activePlansCount: Flow<Int> = userPlanDao.getActivePlansCount()
    val pendingDepositsCount: Flow<Int> = depositDao.getPendingDepositsCount()
    val approvedDepositsCount: Flow<Int> = depositDao.getApprovedDepositsCount()
    val rejectedDepositsCount: Flow<Int> = depositDao.getRejectedDepositsCount()
    val pendingWithdrawalsCount: Flow<Int> = withdrawalDao.getPendingWithdrawalsCount()
    val approvedWithdrawalsCount: Flow<Int> = withdrawalDao.getApprovedWithdrawalsCount()
    val rejectedWithdrawalsCount: Flow<Int> = withdrawalDao.getRejectedWithdrawalsCount()
    val totalTransactionsCount: Flow<Int> = transactionDao.getTotalTransactionCount()
    val totalApprovedDepositAmount: Flow<Double?> = depositDao.getTotalApprovedDepositAmount()
    val totalApprovedWithdrawalAmount: Flow<Double?> = withdrawalDao.getTotalApprovedWithdrawalAmount()

    /**
     * REGISTRATION = COMPLETELY FREE!
     * - NO registration fee.
     * - NO automatic plan purchase (activePlanId = null).
     * - NO 500 plan assigned.
     * - NO automatic deposit created.
     * - NO automatic balance (balance = 0.0).
     * - NO automatic earnings (dailyReturn = 0.0).
     * - Unique User ID generated.
     */
    suspend fun registerUser(
        name: String,
        phone: String,
        password: String,
        email: String?
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val trimmedPhone = phone.trim()
            val existing = userDao.getUserByPhone(trimmedPhone)
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with phone $trimmedPhone already exists. Please log in."))
            }

            // Generate unique user ID
            var generatedUserId: String
            do {
                val randDigits = (10000 + Random.nextInt(90000)).toString()
                generatedUserId = "USR-$randDigits"
            } while (userDao.getUserByUserIdOnce(generatedUserId) != null)

            val newUser = UserEntity(
                userId = generatedUserId,
                name = name.trim(),
                phone = trimmedPhone,
                email = email?.takeIf { it.isNotBlank() }?.trim(),
                passwordHash = SecurityUtil.hashPassword(password),
                balance = 0.0,             // FREE: 0 balance
                status = "Registered",     // Account status: Registered
                activePlanId = null,       // NO AUTOMATIC PLAN
                activePlanName = null,     // NO AUTOMATIC PLAN
                dailyReturn = 0.0,         // NO AUTOMATIC EARNINGS
                lastEarningDate = null,
                registeredAt = System.currentTimeMillis()
            )

            userDao.insertUser(newUser)

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = generatedUserId,
                    action = "USER_REGISTERED_FREE",
                    details = "User ${newUser.name} ($generatedUserId) registered for free. No plan assigned, 0 balance."
                )
            )

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(phone: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByPhone(phone.trim())
                ?: return@withContext Result.failure(Exception("No account found with phone $phone."))

            if (!SecurityUtil.verifyPassword(password, user.passwordHash)) {
                return@withContext Result.failure(Exception("Incorrect password. Please try again."))
            }

            if (user.status == "Suspended") {
                return@withContext Result.failure(Exception("Your account is currently suspended. Please contact administrator."))
            }

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = user.userId,
                    action = "USER_LOGIN",
                    details = "User ${user.userId} logged in successfully."
                )
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin Authentication
     * Initial Admin Password: 451040600
     */
    suspend fun loginAdmin(password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            var adminCreds = adminDao.getAdminCredentials()
            if (adminCreds == null) {
                // Ensure initial credentials seeded: 451040600
                val defaultHash = SecurityUtil.hashPassword("451040600")
                adminCreds = AdminCredentialsEntity(
                    id = 1,
                    username = "admin",
                    passwordHash = defaultHash,
                    updatedAt = System.currentTimeMillis()
                )
                adminDao.saveAdminCredentials(adminCreds)
            }

            if (SecurityUtil.verifyPassword(password, adminCreds.passwordHash)) {
                auditLogDao.insertLog(
                    AuditLogEntity(
                        userId = "ADMIN",
                        action = "ADMIN_LOGIN_SUCCESS",
                        details = "Admin logged into Admin Dashboard successfully."
                    )
                )
                Result.success(true)
            } else {
                auditLogDao.insertLog(
                    AuditLogEntity(
                        userId = "ADMIN",
                        action = "ADMIN_LOGIN_FAILED",
                        details = "Failed admin login attempt with incorrect credentials."
                    )
                )
                Result.failure(Exception("Invalid Admin Password! Please enter the correct admin password."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAdminPassword(oldPassword: String, newPassword: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val adminCreds = adminDao.getAdminCredentials()
                ?: return@withContext Result.failure(Exception("Admin credentials not found."))

            if (!SecurityUtil.verifyPassword(oldPassword, adminCreds.passwordHash)) {
                return@withContext Result.failure(Exception("Current Admin Password is incorrect."))
            }

            if (newPassword.length < 6) {
                return@withContext Result.failure(Exception("New password must be at least 6 characters long."))
            }

            val newHash = SecurityUtil.hashPassword(newPassword)
            adminDao.updatePassword(newHash, System.currentTimeMillis())

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = "ADMIN",
                    action = "ADMIN_PASSWORD_UPDATED",
                    details = "Admin credentials updated successfully."
                )
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit Deposit Request (MANUAL SELECTION ONLY)
     * Receives plan, sender phone, and JazzCash TID.
     * Deposit created as "Pending". NO AUTOMATIC ACTIVATION.
     */
    suspend fun submitDepositRequest(
        user: UserEntity,
        plan: InvestmentPlan,
        senderPhone: String,
        trxReference: String
    ): Result<DepositEntity> = withContext(Dispatchers.IO) {
        try {
            if (trxReference.isBlank()) {
                return@withContext Result.failure(Exception("Please enter the JazzCash Transaction ID (TID)."))
            }
            if (senderPhone.isBlank()) {
                return@withContext Result.failure(Exception("Please enter your JazzCash sender mobile number."))
            }

            val depositId = "DEP-${10000 + Random.nextInt(90000)}"
            val deposit = DepositEntity(
                depositId = depositId,
                userId = user.userId,
                userName = user.name,
                userPhone = user.phone,
                planId = plan.planId,
                planName = plan.name,
                amount = plan.price,
                paymentMethod = "JazzCash",
                recipientAccount = "03227422095",
                senderPhone = senderPhone.trim(),
                transactionReference = trxReference.trim(),
                status = "Pending",
                createdAt = System.currentTimeMillis()
            )

            depositDao.insertDeposit(deposit)

            // Log pending transaction
            val txnId = "TXN-${100000 + Random.nextInt(900000)}"
            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = txnId,
                    userId = user.userId,
                    type = "DEPOSIT",
                    amount = plan.price,
                    status = "Pending",
                    description = "Deposit request submitted for ${plan.name} via JazzCash (TID: $trxReference)",
                    createdAt = System.currentTimeMillis()
                )
            )

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = user.userId,
                    action = "DEPOSIT_SUBMITTED",
                    details = "User ${user.userId} submitted Rs. ${plan.price} deposit request for ${plan.name} (TID: $trxReference). Awaiting Admin approval."
                )
            )

            Result.success(deposit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin APPROVE Deposit
     * - Marks deposit "Approved"
     * - Activates selected plan for this specific user ONLY
     * - Inserts UserPlanEntity
     * - Inserts Completed Transaction
     * - Updates user status to "Active"
     */
    suspend fun approveDeposit(depositId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val deposit = depositDao.getDepositByDepositId(depositId)
                ?: return@withContext Result.failure(Exception("Deposit not found."))

            if (deposit.status == "Approved") {
                return@withContext Result.failure(Exception("Deposit is already approved."))
            }

            val plan = InvestmentPlan.findById(deposit.planId)
            val dailyReturn = plan?.dailyReturn ?: (deposit.amount * 0.2)

            // 1. Update deposit status
            depositDao.updateDepositStatus(
                depositId = depositId,
                status = "Approved",
                reviewedAt = System.currentTimeMillis(),
                reason = "Approved by Admin"
            )

            // 2. Activate user plan for this user ONLY
            userDao.updateActivePlan(
                userId = deposit.userId,
                planId = deposit.planId,
                planName = deposit.planName,
                dailyReturn = dailyReturn
            )
            userDao.updateStatus(deposit.userId, "Active")

            // 3. Insert user plan record
            userPlanDao.insertUserPlan(
                UserPlanEntity(
                    userId = deposit.userId,
                    planId = deposit.planId,
                    planName = deposit.planName,
                    amount = deposit.amount,
                    dailyReturn = dailyReturn,
                    status = "Active",
                    depositReference = deposit.transactionReference,
                    activatedAt = System.currentTimeMillis()
                )
            )

            // 4. Log transaction
            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = "TXN-${100000 + Random.nextInt(900000)}",
                    userId = deposit.userId,
                    type = "DEPOSIT",
                    amount = deposit.amount,
                    status = "Completed",
                    description = "Deposit Approved: ${deposit.planName} activated (JazzCash TID: ${deposit.transactionReference})",
                    createdAt = System.currentTimeMillis()
                )
            )

            // 5. Audit Log
            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = deposit.userId,
                    action = "DEPOSIT_APPROVED",
                    details = "Admin approved deposit $depositId (Rs. ${deposit.amount}). ${deposit.planName} activated for User ${deposit.userId}."
                )
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin REJECT Deposit
     */
    suspend fun rejectDeposit(depositId: String, reason: String = "Verification failed"): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val deposit = depositDao.getDepositByDepositId(depositId)
                ?: return@withContext Result.failure(Exception("Deposit not found."))

            depositDao.updateDepositStatus(
                depositId = depositId,
                status = "Rejected",
                reviewedAt = System.currentTimeMillis(),
                reason = reason
            )

            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = "TXN-${100000 + Random.nextInt(900000)}",
                    userId = deposit.userId,
                    type = "DEPOSIT",
                    amount = deposit.amount,
                    status = "Failed",
                    description = "Deposit Rejected by Admin: $reason (TID: ${deposit.transactionReference})",
                    createdAt = System.currentTimeMillis()
                )
            )

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = deposit.userId,
                    action = "DEPOSIT_REJECTED",
                    details = "Admin rejected deposit $depositId (Rs. ${deposit.amount}). Reason: $reason"
                )
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * User Submit Withdrawal Request
     */
    suspend fun submitWithdrawalRequest(
        user: UserEntity,
        amount: Double,
        method: String,
        accountTitle: String,
        accountNumber: String
    ): Result<WithdrawalEntity> = withContext(Dispatchers.IO) {
        try {
            if (amount < 200.0) {
                return@withContext Result.failure(Exception("Minimum withdrawal amount is Rs. 200."))
            }

            val currentUser = userDao.getUserByUserIdOnce(user.userId)
                ?: return@withContext Result.failure(Exception("User not found."))

            if (currentUser.balance < amount) {
                return@withContext Result.failure(Exception("Insufficient balance. Available: Rs. ${currentUser.balance}"))
            }

            if (accountTitle.isBlank() || accountNumber.isBlank()) {
                return@withContext Result.failure(Exception("Please enter valid account title and account number."))
            }

            // Deduct balance
            val newBalance = currentUser.balance - amount
            userDao.updateBalance(user.userId, newBalance)

            val withdrawalId = "WTH-${10000 + Random.nextInt(90000)}"
            val withdrawal = WithdrawalEntity(
                withdrawalId = withdrawalId,
                userId = user.userId,
                userName = user.name,
                userPhone = user.phone,
                amount = amount,
                paymentMethod = method,
                accountTitle = accountTitle.trim(),
                accountNumber = accountNumber.trim(),
                status = "Pending",
                createdAt = System.currentTimeMillis()
            )

            withdrawalDao.insertWithdrawal(withdrawal)

            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = "TXN-${100000 + Random.nextInt(900000)}",
                    userId = user.userId,
                    type = "WITHDRAWAL",
                    amount = amount,
                    status = "Pending",
                    description = "Withdrawal request submitted via $method to $accountNumber ($accountTitle)",
                    createdAt = System.currentTimeMillis()
                )
            )

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = user.userId,
                    action = "WITHDRAWAL_SUBMITTED",
                    details = "User ${user.userId} submitted Rs. $amount withdrawal via $method to $accountNumber."
                )
            )

            Result.success(withdrawal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin APPROVE Withdrawal
     */
    suspend fun approveWithdrawal(withdrawalId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
                ?: return@withContext Result.failure(Exception("Withdrawal not found."))

            if (withdrawal.status == "Approved") {
                return@withContext Result.failure(Exception("Withdrawal is already approved."))
            }

            withdrawalDao.updateWithdrawalStatus(
                withdrawalId = withdrawalId,
                status = "Approved",
                reviewedAt = System.currentTimeMillis(),
                reason = "Processed by Admin"
            )

            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = "TXN-${100000 + Random.nextInt(900000)}",
                    userId = withdrawal.userId,
                    type = "WITHDRAWAL",
                    amount = withdrawal.amount,
                    status = "Completed",
                    description = "Withdrawal Approved & Sent via ${withdrawal.paymentMethod} to ${withdrawal.accountNumber} (${withdrawal.accountTitle})",
                    createdAt = System.currentTimeMillis()
                )
            )

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = withdrawal.userId,
                    action = "WITHDRAWAL_APPROVED",
                    details = "Admin approved withdrawal $withdrawalId (Rs. ${withdrawal.amount}) for User ${withdrawal.userId}."
                )
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin REJECT Withdrawal (Refunds user balance!)
     */
    suspend fun rejectWithdrawal(withdrawalId: String, reason: String = "Rejected by Admin"): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
                ?: return@withContext Result.failure(Exception("Withdrawal not found."))

            if (withdrawal.status != "Pending") {
                return@withContext Result.failure(Exception("Only pending withdrawals can be rejected."))
            }

            // Refund balance to user
            val user = userDao.getUserByUserIdOnce(withdrawal.userId)
            if (user != null) {
                userDao.updateBalance(withdrawal.userId, user.balance + withdrawal.amount)
            }

            withdrawalDao.updateWithdrawalStatus(
                withdrawalId = withdrawalId,
                status = "Rejected",
                reviewedAt = System.currentTimeMillis(),
                reason = reason
            )

            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = "TXN-${100000 + Random.nextInt(900000)}",
                    userId = withdrawal.userId,
                    type = "WITHDRAWAL",
                    amount = withdrawal.amount,
                    status = "Failed",
                    description = "Withdrawal Rejected & Refunded: $reason",
                    createdAt = System.currentTimeMillis()
                )
            )

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = withdrawal.userId,
                    action = "WITHDRAWAL_REJECTED",
                    details = "Admin rejected withdrawal $withdrawalId (Rs. ${withdrawal.amount}) and refunded balance. Reason: $reason"
                )
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Claim Daily Earnings
     * Only available when user has an active approved plan and hasn't claimed today.
     */
    suspend fun claimDailyEarnings(userId: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByUserIdOnce(userId)
                ?: return@withContext Result.failure(Exception("User not found."))

            if (user.activePlanId == null || user.dailyReturn <= 0) {
                return@withContext Result.failure(Exception("You have no active investment plan. Please select a plan to start earning."))
            }

            val today = DateUtil.getTodayDateString()
            if (user.lastEarningDate == today) {
                return@withContext Result.failure(Exception("You have already claimed today's return (Rs. ${user.dailyReturn}). Come back tomorrow!"))
            }

            val newBalance = user.balance + user.dailyReturn
            userDao.updateDailyEarning(userId, today, newBalance)

            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = "TXN-${100000 + Random.nextInt(900000)}",
                    userId = userId,
                    type = "DAILY_RETURN",
                    amount = user.dailyReturn,
                    status = "Completed",
                    description = "Daily profit credited for ${user.activePlanName ?: "Plan"}",
                    createdAt = System.currentTimeMillis()
                )
            )

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = userId,
                    action = "DAILY_RETURN_CLAIMED",
                    details = "User $userId claimed daily return of Rs. ${user.dailyReturn}. New balance: Rs. $newBalance"
                )
            )

            Result.success(user.dailyReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin Action: Adjust user balance manually
     */
    suspend fun adjustUserBalance(userId: String, newBalance: Double, reason: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByUserIdOnce(userId)
                ?: return@withContext Result.failure(Exception("User not found."))

            userDao.updateBalance(userId, newBalance)

            transactionDao.insertTransaction(
                TransactionEntity(
                    transactionId = "TXN-${100000 + Random.nextInt(900000)}",
                    userId = userId,
                    type = "ADMIN_ADJUST",
                    amount = newBalance - user.balance,
                    status = "Completed",
                    description = "Admin Balance Adjustment: $reason (Old: Rs. ${user.balance}, New: Rs. $newBalance)",
                    createdAt = System.currentTimeMillis()
                )
            )

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = userId,
                    action = "ADMIN_BALANCE_ADJUSTED",
                    details = "Admin set balance to Rs. $newBalance for User $userId. Reason: $reason"
                )
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin Action: Toggle User Status (Active / Suspended)
     */
    suspend fun toggleUserStatus(userId: String, status: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            userDao.updateStatus(userId, status)
            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = userId,
                    action = "ADMIN_STATUS_UPDATED",
                    details = "Admin changed User $userId status to $status."
                )
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin Action: Reset User Password
     */
    suspend fun resetUserPassword(userId: String, newPass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByUserIdOnce(userId)
                ?: return@withContext Result.failure(Exception("User not found."))

            val newHash = SecurityUtil.hashPassword(newPass)
            userDao.updateUser(user.copy(passwordHash = newHash))

            auditLogDao.insertLog(
                AuditLogEntity(
                    userId = userId,
                    action = "ADMIN_USER_PASSWORD_RESET",
                    details = "Admin reset password for User $userId."
                )
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
