package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.DepositEntity
import com.example.data.model.InvestmentPlan
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CurrentScreen {
    object Login : CurrentScreen
    object Register : CurrentScreen
    object UserHome : CurrentScreen
    object AdminLogin : CurrentScreen
    object AdminHome : CurrentScreen
}

enum class AdminTab {
    DASHBOARD,
    ALL_USERS,
    DEPOSITS,
    WITHDRAWALS,
    TRANSACTIONS,
    PLANS,
    APPROVE_REJECT_REQUESTS,
    ADMIN_ACTIONS,
    ADMIN_SETTINGS
}

data class UiMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = AppRepository(db)

    // Current navigation state
    private val _currentScreen = MutableStateFlow<CurrentScreen>(CurrentScreen.Login)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    // Logged in User state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Admin state
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _currentAdminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val currentAdminTab: StateFlow<AdminTab> = _currentAdminTab.asStateFlow()

    // Selected user for Admin details / history view
    private val _selectedAdminUser = MutableStateFlow<UserEntity?>(null)
    val selectedAdminUser: StateFlow<UserEntity?> = _selectedAdminUser.asStateFlow()

    // UI Message / Toast / Snackbar
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    // Central Data Streams from Room DB
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits: StateFlow<List<DepositEntity>> = repository.allDeposits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.allWithdrawals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Statistics from Room DB
    val totalUsersCount: StateFlow<Int> = repository.totalUsersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val usersWithoutPlanCount: StateFlow<Int> = repository.usersWithoutPlanCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activePlansCount: StateFlow<Int> = repository.activePlansCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingDepositsCount: StateFlow<Int> = repository.pendingDepositsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val approvedDepositsCount: StateFlow<Int> = repository.approvedDepositsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rejectedDepositsCount: StateFlow<Int> = repository.rejectedDepositsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingWithdrawalsCount: StateFlow<Int> = repository.pendingWithdrawalsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val approvedWithdrawalsCount: StateFlow<Int> = repository.approvedWithdrawalsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rejectedWithdrawalsCount: StateFlow<Int> = repository.rejectedWithdrawalsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTransactionsCount: StateFlow<Int> = repository.totalTransactionsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalApprovedDepositAmount: StateFlow<Double?> = repository.totalApprovedDepositAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalApprovedWithdrawalAmount: StateFlow<Double?> = repository.totalApprovedWithdrawalAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Keep currentUser refreshed reactively when database changes
    init {
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                val current = _currentUser.value
                if (current != null) {
                    val updated = users.find { it.userId == current.userId }
                    if (updated != null) {
                        _currentUser.value = updated
                    }
                }
                val selected = _selectedAdminUser.value
                if (selected != null) {
                    _selectedAdminUser.value = users.find { it.userId == selected.userId }
                }
            }
        }
    }

    fun navigateTo(screen: CurrentScreen) {
        _currentScreen.value = screen
    }

    fun setAdminTab(tab: AdminTab) {
        _currentAdminTab.value = tab
    }

    fun selectAdminUser(user: UserEntity?) {
        _selectedAdminUser.value = user
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String, isError: Boolean = false) {
        _uiMessage.value = UiMessage(message = msg, isError = isError)
    }

    /**
     * FREE REGISTRATION
     */
    fun registerUser(name: String, phone: String, pass: String, email: String?) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank() || pass.isBlank()) {
                showMessage("Please fill in Name, Phone, and Password", isError = true)
                return@launch
            }
            if (pass.length < 4) {
                showMessage("Password must be at least 4 characters", isError = true)
                return@launch
            }

            val result = repository.registerUser(name, phone, pass, email)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentScreen.value = CurrentScreen.UserHome
                showMessage("Account created successfully! Registration is 100% Free.")
            }.onFailure { err ->
                showMessage(err.message ?: "Registration failed", isError = true)
            }
        }
    }

    fun loginUser(phone: String, pass: String) {
        viewModelScope.launch {
            if (phone.isBlank() || pass.isBlank()) {
                showMessage("Please enter Phone Number and Password", isError = true)
                return@launch
            }
            val result = repository.loginUser(phone, pass)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentScreen.value = CurrentScreen.UserHome
                showMessage("Welcome back, ${user.name}!")
            }.onFailure { err ->
                showMessage(err.message ?: "Login failed", isError = true)
            }
        }
    }

    fun logoutUser() {
        _currentUser.value = null
        _currentScreen.value = CurrentScreen.Login
        showMessage("Logged out successfully.")
    }

    /**
     * Admin Login
     */
    fun loginAdmin(password: String) {
        viewModelScope.launch {
            if (password.isBlank()) {
                showMessage("Please enter Admin Password", isError = true)
                return@launch
            }
            val result = repository.loginAdmin(password)
            result.onSuccess {
                _isAdminLoggedIn.value = true
                _currentScreen.value = CurrentScreen.AdminHome
                _currentAdminTab.value = AdminTab.DASHBOARD
                showMessage("Admin Login Successful. Welcome to Central Admin Dashboard.")
            }.onFailure { err ->
                showMessage(err.message ?: "Admin authentication failed", isError = true)
            }
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _currentScreen.value = CurrentScreen.Login
        showMessage("Admin logged out.")
    }

    fun changeAdminPassword(oldPass: String, newPass: String, confirmPass: String) {
        viewModelScope.launch {
            if (newPass != confirmPass) {
                showMessage("New passwords do not match", isError = true)
                return@launch
            }
            val res = repository.updateAdminPassword(oldPass, newPass)
            res.onSuccess {
                showMessage("Admin password updated successfully!")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to update password", isError = true)
            }
        }
    }

    /**
     * User Deposit Request (Manual selection)
     */
    fun submitDeposit(plan: InvestmentPlan, senderPhone: String, trxId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.submitDepositRequest(user, plan, senderPhone, trxId)
            res.onSuccess {
                showMessage("Deposit request submitted! Awaiting Admin verification and approval.")
            }.onFailure { err ->
                showMessage(err.message ?: "Deposit request failed", isError = true)
            }
        }
    }

    /**
     * User Withdrawal Request
     */
    fun submitWithdrawal(amount: Double, method: String, title: String, number: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.submitWithdrawalRequest(user, amount, method, title, number)
            res.onSuccess {
                showMessage("Withdrawal request submitted! Sent to Admin for review.")
            }.onFailure { err ->
                showMessage(err.message ?: "Withdrawal failed", isError = true)
            }
        }
    }

    /**
     * Claim Daily Profit
     */
    fun claimDailyProfit() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.claimDailyEarnings(user.userId)
            res.onSuccess { amount ->
                showMessage("Success! Rs. ${amount.toInt()} daily earnings credited to your balance.")
            }.onFailure { err ->
                showMessage(err.message ?: "Claim failed", isError = true)
            }
        }
    }

    /**
     * Admin Approve Deposit
     */
    fun approveDeposit(depositId: String) {
        viewModelScope.launch {
            val res = repository.approveDeposit(depositId)
            res.onSuccess {
                showMessage("Deposit $depositId APPROVED! Plan activated for user.")
            }.onFailure { err ->
                showMessage(err.message ?: "Approval failed", isError = true)
            }
        }
    }

    /**
     * Admin Reject Deposit
     */
    fun rejectDeposit(depositId: String, reason: String) {
        viewModelScope.launch {
            val res = repository.rejectDeposit(depositId, reason)
            res.onSuccess {
                showMessage("Deposit $depositId REJECTED.")
            }.onFailure { err ->
                showMessage(err.message ?: "Rejection failed", isError = true)
            }
        }
    }

    /**
     * Admin Approve Withdrawal
     */
    fun approveWithdrawal(withdrawalId: String) {
        viewModelScope.launch {
            val res = repository.approveWithdrawal(withdrawalId)
            res.onSuccess {
                showMessage("Withdrawal $withdrawalId APPROVED & Marked Paid.")
            }.onFailure { err ->
                showMessage(err.message ?: "Approval failed", isError = true)
            }
        }
    }

    /**
     * Admin Reject Withdrawal
     */
    fun rejectWithdrawal(withdrawalId: String, reason: String) {
        viewModelScope.launch {
            val res = repository.rejectWithdrawal(withdrawalId, reason)
            res.onSuccess {
                showMessage("Withdrawal $withdrawalId REJECTED. Balance refunded to user.")
            }.onFailure { err ->
                showMessage(err.message ?: "Rejection failed", isError = true)
            }
        }
    }

    /**
     * Admin adjust user balance
     */
    fun adjustUserBalance(userId: String, newBalance: Double, reason: String) {
        viewModelScope.launch {
            val res = repository.adjustUserBalance(userId, newBalance, reason)
            res.onSuccess {
                showMessage("Balance updated to Rs. $newBalance for user $userId.")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to adjust balance", isError = true)
            }
        }
    }

    /**
     * Admin toggle user status
     */
    fun toggleUserStatus(userId: String, newStatus: String) {
        viewModelScope.launch {
            val res = repository.toggleUserStatus(userId, newStatus)
            res.onSuccess {
                showMessage("User $userId status updated to $newStatus.")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to update status", isError = true)
            }
        }
    }

    /**
     * Admin reset user password
     */
    fun resetUserPassword(userId: String, newPass: String) {
        viewModelScope.launch {
            val res = repository.resetUserPassword(userId, newPass)
            res.onSuccess {
                showMessage("Password reset successfully for user $userId.")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to reset password", isError = true)
            }
        }
    }

    /**
     * Helper for quick multi-device / multi-user testing:
     * Switch directly to any registered user in the central database
     */
    fun switchToUser(user: UserEntity) {
        _currentUser.value = user
        _currentScreen.value = CurrentScreen.UserHome
        showMessage("Switched to ${user.name} (${user.userId})")
    }
}
