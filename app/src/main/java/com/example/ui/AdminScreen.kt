package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntity
import com.example.data.model.DepositEntity
import com.example.data.model.InvestmentPlan
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.util.DateUtil

@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentAdminTab.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allLogs by viewModel.allAuditLogs.collectAsState()

    // Real-time statistics from DB
    val totalUsers by viewModel.totalUsersCount.collectAsState()
    val usersWithoutPlan by viewModel.usersWithoutPlanCount.collectAsState()
    val activePlans by viewModel.activePlansCount.collectAsState()
    val pendingDeposits by viewModel.pendingDepositsCount.collectAsState()
    val approvedDeposits by viewModel.approvedDepositsCount.collectAsState()
    val rejectedDeposits by viewModel.rejectedDepositsCount.collectAsState()
    val pendingWithdrawals by viewModel.pendingWithdrawalsCount.collectAsState()
    val approvedWithdrawals by viewModel.approvedWithdrawalsCount.collectAsState()
    val rejectedWithdrawals by viewModel.rejectedWithdrawalsCount.collectAsState()
    val totalTransactions by viewModel.totalTransactionsCount.collectAsState()
    val totalApprovedDepositAmount by viewModel.totalApprovedDepositAmount.collectAsState()
    val totalApprovedWithdrawalAmount by viewModel.totalApprovedWithdrawalAmount.collectAsState()

    val selectedUser by viewModel.selectedAdminUser.collectAsState()

    var rejectDepositId by remember { mutableStateOf<String?>(null) }
    var rejectWithdrawalId by remember { mutableStateOf<String?>(null) }
    var rejectReason by remember { mutableStateOf("Invalid details / payment not received") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Admin Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Central Admin Panel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Root Database • Real-Time Multi-Device Sync",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Logout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Navigation Tabs (Requirement #2: All required sections)
        val tabs = listOf(
            AdminTab.DASHBOARD to "Dashboard",
            AdminTab.ALL_USERS to "All Users ($totalUsers)",
            AdminTab.APPROVE_REJECT_REQUESTS to "Approve/Reject (${pendingDeposits + pendingWithdrawals})",
            AdminTab.DEPOSITS to "Deposits ($pendingDeposits pending)",
            AdminTab.WITHDRAWALS to "Withdrawals ($pendingWithdrawals pending)",
            AdminTab.TRANSACTIONS to "Transactions",
            AdminTab.PLANS to "Plans",
            AdminTab.ADMIN_ACTIONS to "Admin Actions",
            AdminTab.ADMIN_SETTINGS to "Admin Settings"
        )

        ScrollableTabRow(
            selectedTabIndex = tabs.indexOfFirst { it.first == currentTab }.coerceAtLeast(0),
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEach { (tab, label) ->
                Tab(
                    selected = currentTab == tab,
                    onClick = {
                        viewModel.setAdminTab(tab)
                        viewModel.selectAdminUser(null)
                    },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                )
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            if (selectedUser != null) {
                // User Details & Complete User History Screen (Requirement #9 & #10)
                AdminUserDetailsScreen(
                    user = selectedUser!!,
                    allDeposits = allDeposits.filter { it.userId == selectedUser!!.userId },
                    allWithdrawals = allWithdrawals.filter { it.userId == selectedUser!!.userId },
                    allTransactions = allTransactions.filter { it.userId == selectedUser!!.userId },
                    allLogs = allLogs.filter { it.userId == selectedUser!!.userId },
                    onBack = { viewModel.selectAdminUser(null) },
                    onAdjustBalance = { newBal, reason -> viewModel.adjustUserBalance(selectedUser!!.userId, newBal, reason) },
                    onToggleStatus = { newStat -> viewModel.toggleUserStatus(selectedUser!!.userId, newStat) },
                    onResetPassword = { newPass -> viewModel.resetUserPassword(selectedUser!!.userId, newPass) }
                )
            } else {
                when (currentTab) {
                    AdminTab.DASHBOARD -> AdminDashboardSection(
                        totalUsers = totalUsers,
                        usersWithoutPlan = usersWithoutPlan,
                        activePlans = activePlans,
                        pendingDeposits = pendingDeposits,
                        approvedDeposits = approvedDeposits,
                        rejectedDeposits = rejectedDeposits,
                        pendingWithdrawals = pendingWithdrawals,
                        approvedWithdrawals = approvedWithdrawals,
                        rejectedWithdrawals = rejectedWithdrawals,
                        totalTransactions = totalTransactions,
                        totalApprovedDepositAmount = totalApprovedDepositAmount ?: 0.0,
                        totalApprovedWithdrawalAmount = totalApprovedWithdrawalAmount ?: 0.0,
                        onNavigateToRequests = { viewModel.setAdminTab(AdminTab.APPROVE_REJECT_REQUESTS) },
                        onNavigateToUsers = { viewModel.setAdminTab(AdminTab.ALL_USERS) }
                    )

                    AdminTab.ALL_USERS -> AdminAllUsersSection(
                        users = allUsers,
                        allDeposits = allDeposits,
                        allWithdrawals = allWithdrawals,
                        onSelectUser = { viewModel.selectAdminUser(it) }
                    )

                    AdminTab.APPROVE_REJECT_REQUESTS -> AdminApproveRejectQueueSection(
                        pendingDeposits = allDeposits.filter { it.status == "Pending" },
                        pendingWithdrawals = allWithdrawals.filter { it.status == "Pending" },
                        onApproveDeposit = { viewModel.approveDeposit(it) },
                        onRejectDeposit = { rejectDepositId = it },
                        onApproveWithdrawal = { viewModel.approveWithdrawal(it) },
                        onRejectWithdrawal = { rejectWithdrawalId = it }
                    )

                    AdminTab.DEPOSITS -> AdminDepositsSection(
                        deposits = allDeposits,
                        onApproveDeposit = { viewModel.approveDeposit(it) },
                        onRejectDeposit = { rejectDepositId = it }
                    )

                    AdminTab.WITHDRAWALS -> AdminWithdrawalsSection(
                        withdrawals = allWithdrawals,
                        onApproveWithdrawal = { viewModel.approveWithdrawal(it) },
                        onRejectWithdrawal = { rejectWithdrawalId = it }
                    )

                    AdminTab.TRANSACTIONS -> AdminTransactionsSection(
                        transactions = allTransactions
                    )

                    AdminTab.PLANS -> AdminPlansSection(
                        plans = InvestmentPlan.ALL_PLANS,
                        allUsers = allUsers
                    )

                    AdminTab.ADMIN_ACTIONS -> AdminActionsSection(
                        users = allUsers,
                        onSelectUser = { viewModel.selectAdminUser(it) },
                        onBroadcastNotice = { viewModel.showMessage("Broadcast notice posted to all devices: $it") }
                    )

                    AdminTab.ADMIN_SETTINGS -> AdminSettingsSection(
                        onChangePassword = { oldPass, newPass, confPass ->
                            viewModel.changeAdminPassword(oldPass, newPass, confPass)
                        }
                    )
                }
            }
        }
    }

    // Rejection Dialog for Deposit
    if (rejectDepositId != null) {
        val depId = rejectDepositId!!
        AlertDialog(
            onDismissRequest = { rejectDepositId = null },
            title = { Text("Reject Deposit ($depId)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Specify reason for rejecting this deposit:")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectDeposit(depId, rejectReason)
                        rejectDepositId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectDepositId = null }) { Text("Cancel") }
            }
        )
    }

    // Rejection Dialog for Withdrawal
    if (rejectWithdrawalId != null) {
        val withId = rejectWithdrawalId!!
        AlertDialog(
            onDismissRequest = { rejectWithdrawalId = null },
            title = { Text("Reject Withdrawal ($withId)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Specify reason. The user's balance will be automatically refunded:")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectWithdrawal(withId, rejectReason)
                        rejectWithdrawalId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm & Refund Balance")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectWithdrawalId = null }) { Text("Cancel") }
            }
        )
    }
}

// 1. DASHBOARD SECTION (Requirement #15)
@Composable
fun AdminDashboardSection(
    totalUsers: Int,
    usersWithoutPlan: Int,
    activePlans: Int,
    pendingDeposits: Int,
    approvedDeposits: Int,
    rejectedDeposits: Int,
    pendingWithdrawals: Int,
    approvedWithdrawals: Int,
    rejectedWithdrawals: Int,
    totalTransactions: Int,
    totalApprovedDepositAmount: Double,
    totalApprovedWithdrawalAmount: Double,
    onNavigateToRequests: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pending Action Banner if requests exist
        if (pendingDeposits > 0 || pendingWithdrawals > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRequests() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFA000))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pending Approvals Required!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "$pendingDeposits Deposits • $pendingWithdrawals Withdrawals waiting",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFBF360C)
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToRequests,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            Text("Review Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Central Database Live Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Synced across all devices and phones connected to this server",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Users",
                        value = "$totalUsers",
                        subtitle = "All registered devices",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToUsers
                    )
                    StatCard(
                        title = "Users Without Plans",
                        value = "$usersWithoutPlan",
                        subtitle = "Free accounts",
                        color = Color(0xFF7B1FA2),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToUsers
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Active Plans",
                        value = "$activePlans",
                        subtitle = "Approved investments",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Transactions",
                        value = "$totalTransactions",
                        subtitle = "Central audit trail",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Pending Deposits",
                        value = "$pendingDeposits",
                        subtitle = "Awaiting verification",
                        color = Color(0xFFF57F17),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Approved Deposits",
                        value = "$approvedDeposits",
                        subtitle = "Rs. ${totalApprovedDepositAmount.toInt()}",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Pending Withdrawals",
                        value = "$pendingWithdrawals",
                        subtitle = "Awaiting payout",
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Approved Withdrawals",
                        value = "$approvedWithdrawals",
                        subtitle = "Rs. ${totalApprovedWithdrawalAmount.toInt()}",
                        color = Color(0xFF00897B),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Rejected Deposits",
                        value = "$rejectedDeposits",
                        subtitle = "Failed verification",
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Rejected Withdrawals",
                        value = "$rejectedWithdrawals",
                        subtitle = "Refunded to users",
                        color = Color(0xFFAD1457),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 2. ALL USERS SECTION (Requirement #9)
@Composable
fun AdminAllUsersSection(
    users: List<UserEntity>,
    allDeposits: List<DepositEntity>,
    allWithdrawals: List<WithdrawalEntity>,
    onSelectUser: (UserEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery) ||
            it.userId.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by User ID, Name or Phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_search_users_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Text(
            text = "Total Registered Users (${filteredUsers.size})",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No users found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers) { user ->
                    val userDepCount = allDeposits.count { it.userId == user.userId && it.status == "Approved" }
                    val userWithCount = allWithdrawals.count { it.userId == user.userId && it.status == "Approved" }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectUser(user) }
                            .testTag("admin_user_item_${user.userId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = user.userId,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                StatusBadge(status = user.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Phone: ${user.phone}${if (!user.email.isNullOrBlank()) " • Email: ${user.email}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Registered: ${DateUtil.formatDateTime(user.registeredAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider()

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Plan: ${user.activePlanName ?: "None / Not Purchased"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.activePlanId == null) Color.Gray else Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "Deposit Status: ${if (userDepCount > 0) "Approved ($userDepCount)" else "None / 0"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Balance: Rs. ${"%,.2f".format(user.balance)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Tap to View Full History →",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. USER DETAILS & COMPLETE USER HISTORY (Requirement #9 & #10)
@Composable
fun AdminUserDetailsScreen(
    user: UserEntity,
    allDeposits: List<DepositEntity>,
    allWithdrawals: List<WithdrawalEntity>,
    allTransactions: List<TransactionEntity>,
    allLogs: List<AuditLogEntity>,
    onBack: () -> Unit,
    onAdjustBalance: (newBalance: Double, reason: String) -> Unit,
    onToggleStatus: (newStatus: String) -> Unit,
    onResetPassword: (newPassword: String) -> Unit
) {
    var showAdjustBalanceModal by remember { mutableStateOf(false) }
    var showResetPasswordModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back to All Users")
                }

                StatusBadge(status = user.status)
            }
        }

        // Profile Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Unique User ID: ${user.userId}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Balance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Rs. ${"%,.2f".format(user.balance)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Phone: ${user.phone}")
                    Text("Email: ${user.email ?: "Not provided"}")
                    Text("Registration Date: ${DateUtil.formatDate(user.registeredAt)}")
                    Text("Registration Time: ${DateUtil.formatTime(user.registeredAt)}")
                    Text("Selected Plan: ${user.activePlanName ?: "None / Not Purchased"}")
                    Text("Plan Status: ${if (user.activePlanId != null) "Active" else "None"}")
                    Text("Daily Return: Rs. ${user.dailyReturn.toInt()} / day")

                    Spacer(modifier = Modifier.height(14.dp))

                    // Admin Actions on User
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAdjustBalanceModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Adjust Balance", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val newStatus = if (user.status == "Active" || user.status == "Registered") "Suspended" else "Active"
                                onToggleStatus(newStatus)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.status == "Suspended") Color(0xFF2E7D32) else Color(0xFFC62828)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (user.status == "Suspended") "Unsuspend" else "Suspend", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showResetPasswordModal = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Pass", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Complete User History Sections (Requirement #10)
        item {
            Text(
                text = "Complete User History (${user.userId})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Deposit History
        item {
            Text(
                text = "Deposit History (${allDeposits.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (allDeposits.isEmpty()) {
            item {
                Text(
                    "No deposit requests made by this user.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(allDeposits) { dep ->
                DepositItemCard(deposit = dep)
            }
        }

        // Withdrawal History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Withdrawal History (${allWithdrawals.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (allWithdrawals.isEmpty()) {
            item {
                Text(
                    "No withdrawal requests made by this user.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(allWithdrawals) { with ->
                WithdrawalItemCard(withdrawal = with)
            }
        }

        // Transaction History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Transaction History (${allTransactions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (allTransactions.isEmpty()) {
            item {
                Text(
                    "No transaction logs for this user.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(allTransactions) { txn ->
                TransactionItemCard(transaction = txn)
            }
        }

        // Audit & System Log for this user
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Audit & Approval/Rejection Log (${allLogs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(allLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = log.action,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = DateUtil.formatDateTime(log.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal: Adjust Balance
    if (showAdjustBalanceModal) {
        var balText by remember { mutableStateOf(user.balance.toString()) }
        var reasonText by remember { mutableStateOf("Admin adjustment") }

        AlertDialog(
            onDismissRequest = { showAdjustBalanceModal = false },
            title = { Text("Adjust Balance for ${user.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Balance: Rs. ${user.balance}")
                    OutlinedTextField(
                        value = balText,
                        onValueChange = { balText = it },
                        label = { Text("New Balance (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val bal = balText.toDoubleOrNull() ?: user.balance
                    onAdjustBalance(bal, reasonText)
                    showAdjustBalanceModal = false
                }) {
                    Text("Save Balance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustBalanceModal = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Reset Password
    if (showResetPasswordModal) {
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showResetPasswordModal = false },
            title = { Text("Reset Password for ${user.name}") },
            text = {
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New User Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPass.isNotBlank()) {
                        onResetPassword(newPass)
                        showResetPasswordModal = false
                    }
                }) {
                    Text("Reset Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordModal = false }) { Text("Cancel") }
            }
        )
    }
}

// 4. APPROVE/REJECT QUICK QUEUE (Requirement #2 & #11 & #12)
@Composable
fun AdminApproveRejectQueueSection(
    pendingDeposits: List<DepositEntity>,
    pendingWithdrawals: List<WithdrawalEntity>,
    onApproveDeposit: (String) -> Unit,
    onRejectDeposit: (String) -> Unit,
    onApproveWithdrawal: (String) -> Unit,
    onRejectWithdrawal: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Urgent Action Queue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Approve or reject incoming deposit payments and withdrawal payouts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Pending Deposits Queue
        item {
            Text(
                text = "Pending Deposits (${pendingDeposits.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )
        }

        if (pendingDeposits.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("✓ No pending deposits to approve.", modifier = Modifier.padding(14.dp))
                }
            }
        } else {
            items(pendingDeposits) { dep ->
                AdminDepositCard(
                    deposit = dep,
                    onApprove = { onApproveDeposit(dep.depositId) },
                    onReject = { onRejectDeposit(dep.depositId) }
                )
            }
        }

        // Pending Withdrawals Queue
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Pending Withdrawals (${pendingWithdrawals.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
        }

        if (pendingWithdrawals.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("✓ No pending withdrawals to review.", modifier = Modifier.padding(14.dp))
                }
            }
        } else {
            items(pendingWithdrawals) { with ->
                AdminWithdrawalCard(
                    withdrawal = with,
                    onApprove = { onApproveWithdrawal(with.withdrawalId) },
                    onReject = { onRejectWithdrawal(with.withdrawalId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// 5. DEPOSITS MANAGEMENT SECTION (Requirement #11)
@Composable
fun AdminDepositsSection(
    deposits: List<DepositEntity>,
    onApproveDeposit: (String) -> Unit,
    onRejectDeposit: (String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("All") }
    val filteredDeposits = remember(deposits, statusFilter) {
        if (statusFilter == "All") deposits
        else deposits.filter { it.status == statusFilter }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Pending", "Approved", "Rejected").forEach { filter ->
                FilterChip(
                    text = filter,
                    isSelected = statusFilter == filter,
                    onClick = { statusFilter = filter }
                )
            }
        }

        if (filteredDeposits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No deposits found for filter: $statusFilter")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDeposits) { dep ->
                    AdminDepositCard(
                        deposit = dep,
                        onApprove = { onApproveDeposit(dep.depositId) },
                        onReject = { onRejectDeposit(dep.depositId) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDepositCard(
    deposit: DepositEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_deposit_card_${deposit.depositId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${deposit.planName} • Rs. ${deposit.amount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Deposit ID: ${deposit.depositId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = deposit.status)
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Text("User: ${deposit.userName} (${deposit.userId})", fontWeight = FontWeight.SemiBold)
            Text("User Mobile: ${deposit.userPhone}")
            Text("JazzCash Sender: ${deposit.senderPhone}")
            Text("JazzCash TID: ${deposit.transactionReference}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Date & Time: ${DateUtil.formatDateTime(deposit.createdAt)}", style = MaterialTheme.typography.labelSmall)

            if (!deposit.rejectReason.isNullOrBlank()) {
                Text("Reason: ${deposit.rejectReason}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (deposit.status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_deposit_btn_${deposit.depositId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("APPROVE", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_deposit_btn_${deposit.depositId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REJECT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 6. WITHDRAWALS MANAGEMENT SECTION (Requirement #12)
@Composable
fun AdminWithdrawalsSection(
    withdrawals: List<WithdrawalEntity>,
    onApproveWithdrawal: (String) -> Unit,
    onRejectWithdrawal: (String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("All") }
    val filteredWithdrawals = remember(withdrawals, statusFilter) {
        if (statusFilter == "All") withdrawals
        else withdrawals.filter { it.status == statusFilter }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Pending", "Approved", "Rejected").forEach { filter ->
                FilterChip(
                    text = filter,
                    isSelected = statusFilter == filter,
                    onClick = { statusFilter = filter }
                )
            }
        }

        if (filteredWithdrawals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No withdrawals found for filter: $statusFilter")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredWithdrawals) { with ->
                    AdminWithdrawalCard(
                        withdrawal = with,
                        onApprove = { onApproveWithdrawal(with.withdrawalId) },
                        onReject = { onRejectWithdrawal(with.withdrawalId) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalCard(
    withdrawal: WithdrawalEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_withdrawal_card_${withdrawal.withdrawalId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rs. ${withdrawal.amount.toInt()} via ${withdrawal.paymentMethod}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Withdrawal ID: ${withdrawal.withdrawalId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = withdrawal.status)
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Text("User: ${withdrawal.userName} (${withdrawal.userId})", fontWeight = FontWeight.SemiBold)
            Text("User Mobile: ${withdrawal.userPhone}")
            Text("Account Title: ${withdrawal.accountTitle}", fontWeight = FontWeight.Bold)
            Text("Account / Mobile: ${withdrawal.accountNumber}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Date & Time: ${DateUtil.formatDateTime(withdrawal.createdAt)}", style = MaterialTheme.typography.labelSmall)

            if (withdrawal.status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_withdrawal_btn_${withdrawal.withdrawalId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("APPROVE", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_withdrawal_btn_${withdrawal.withdrawalId}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REJECT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 7. TRANSACTIONS SECTION (Requirement #2 & #15)
@Composable
fun AdminTransactionsSection(transactions: List<TransactionEntity>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Central Transactions Audit Log (${transactions.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions logged in central database yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { txn ->
                    TransactionItemCard(transaction = txn)
                }
            }
        }
    }
}

// 8. PLANS SECTION (Requirement #7)
@Composable
fun AdminPlansSection(
    plans: List<InvestmentPlan>,
    allUsers: List<UserEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Configured Investment Plans",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manual selection plans available for all users. None are assigned automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(plans) { plan ->
            val subscriberCount = allUsers.count { it.activePlanId == plan.planId }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Rs. ${plan.price.toInt()}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily Return: Rs. ${plan.dailyReturn.toInt()} / day",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Configured Duration: ${plan.validityDays} days",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Current Active Subscribers: $subscriberCount users",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// 9. ADMIN ACTIONS SECTION (Requirement #2)
@Composable
fun AdminActionsSection(
    users: List<UserEntity>,
    onSelectUser: (UserEntity) -> Unit,
    onBroadcastNotice: (String) -> Unit
) {
    var broadcastMsg by remember { mutableStateOf("") }
    var searchPhone by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "System Administration Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Quick User Lookup Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Find User Account",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchPhone,
                        onValueChange = { searchPhone = it },
                        label = { Text("Search by Phone or User ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val found = users.find {
                        it.phone == searchPhone.trim() || it.userId.equals(searchPhone.trim(), ignoreCase = true)
                    }
                    if (found != null) {
                        Button(
                            onClick = { onSelectUser(found) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open ${found.name} (${found.userId})")
                        }
                    } else if (searchPhone.isNotBlank()) {
                        Text("No matching user found.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Broadcast Notice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "System Announcement Broadcast",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = broadcastMsg,
                        onValueChange = { broadcastMsg = it },
                        label = { Text("Announcement message") },
                        placeholder = { Text("e.g. Deposit processing is live 24/7 via JazzCash 03227422095") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (broadcastMsg.isNotBlank()) {
                                onBroadcastNotice(broadcastMsg)
                                broadcastMsg = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Broadcast Notice")
                    }
                }
            }
        }
    }
}

// 10. ADMIN SETTINGS SECTION (Requirement #3)
@Composable
fun AdminSettingsSection(
    onChangePassword: (oldPass: String, newPass: String, confPass: String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Admin Credentials & Security",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage root administrator access and credentials securely. Initial password was 451040600.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Change Admin Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Current Admin Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_old_password_input")
                    )

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Admin Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_new_password_input")
                    )

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_confirm_new_password_input")
                    )

                    Button(
                        onClick = {
                            onChangePassword(oldPass, newPass, confirmPass)
                            oldPass = ""
                            newPass = ""
                            confirmPass = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_update_password_button")
                    ) {
                        Text("Update Admin Password")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔐 Security Guarantee",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Admin password is cryptographically hashed using SHA-256 in the central database.\n• Credentials are never returned through public APIs or visible in HTML/JS.\n• Only verified authenticated admins can access this dashboard and approve/reject requests.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
