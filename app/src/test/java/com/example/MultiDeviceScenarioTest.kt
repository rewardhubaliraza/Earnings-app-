package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.InvestmentPlan
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MultiDeviceScenarioTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testRegistrationIsCompletelyFree_NoAutoPlan_NoAutoDeposit() = runBlocking {
        // Register new user
        val regResult = repository.registerUser(
            name = "Test User A",
            phone = "03001111111",
            password = "password123",
            email = "userA@example.com"
        )

        assertTrue("Registration must succeed", regResult.isSuccess)
        val user = regResult.getOrThrow()

        // Verify Requirement #4 & #5:
        // 1. Unique User ID generated
        assertTrue("User ID must start with USR-", user.userId.startsWith("USR-"))
        // 2. NO plan is purchased or active
        assertNull("activePlanId MUST be null on registration", user.activePlanId)
        assertNull("activePlanName MUST be null on registration", user.activePlanName)
        // 3. Balance must be 0.0
        assertEquals("Initial balance must be 0.0", 0.0, user.balance, 0.001)
        // 4. Daily return must be 0.0
        assertEquals("Initial daily return must be 0.0", 0.0, user.dailyReturn, 0.001)
        // 5. Status must be Registered
        assertEquals("Status must be Registered", "Registered", user.status)

        // Verify deposits table has 0 deposits for this user
        val deposits = repository.depositDao.getDepositsByUserId(user.userId).first()
        assertEquals("Newly registered user must have 0 deposits", 0, deposits.size)

        // Verify user_plans table has 0 plans for this user
        val plans = repository.userPlanDao.getUserPlansByUserId(user.userId).first()
        assertEquals("Newly registered user must have 0 user_plans", 0, plans.size)
    }

    @Test
    fun testAdminInitialPasswordAuthentication() = runBlocking {
        // Initial Admin Password from requirements: 451040600
        val failResult = repository.loginAdmin("wrongPassword")
        assertFalse("Wrong admin password must fail", failResult.isSuccess)

        val successResult = repository.loginAdmin("451040600")
        assertTrue("Correct initial password 451040600 must succeed", successResult.isSuccess)
    }

    @Test
    fun testRequirement16_MultiDeviceScenario() = runBlocking {
        // PHONE A: Register User A
        val userAResult = repository.registerUser(
            name = "User A",
            phone = "03001111111",
            password = "passA",
            email = "usera@test.com"
        )
        assertTrue(userAResult.isSuccess)
        val userA = userAResult.getOrThrow()

        // Expected: Registration succeeds for FREE. No plan, no 500 plan, no deposit.
        assertNull(userA.activePlanId)
        assertEquals(0.0, userA.balance, 0.001)

        // PHONE B: Register User B
        val userBResult = repository.registerUser(
            name = "User B",
            phone = "03002222222",
            password = "passB",
            email = "userb@test.com"
        )
        assertTrue(userBResult.isSuccess)
        val userB = userBResult.getOrThrow()

        // Expected: Same behavior for User B.
        assertNull(userB.activePlanId)
        assertEquals(0.0, userB.balance, 0.001)

        // ADMIN PHONE: Check All Users
        val allUsers = repository.allUsers.first()
        assertEquals("Both User A and User B must appear in All Users", 2, allUsers.size)

        val fetchedUserA = allUsers.find { it.userId == userA.userId }
        val fetchedUserB = allUsers.find { it.userId == userB.userId }
        assertNotNull(fetchedUserA)
        assertNotNull(fetchedUserB)

        // Both users initially show: Plan: None, Deposit: 0 / None
        assertNull("User A Plan must be None", fetchedUserA?.activePlanId)
        assertNull("User B Plan must be None", fetchedUserB?.activePlanId)

        // USER A manually selects 500 Plan and submits deposit request
        val plan500 = InvestmentPlan.findById(500)!!
        assertEquals(500.0, plan500.price, 0.001)
        assertEquals(100.0, plan500.dailyReturn, 0.001)

        val depositResult = repository.submitDepositRequest(
            user = fetchedUserA!!,
            plan = plan500,
            senderPhone = "03001111111",
            trxReference = "JAZZ-TRX-987654"
        )
        assertTrue("User A deposit request submission must succeed", depositResult.isSuccess)
        val depositA = depositResult.getOrThrow()

        // Verify: Only User A gets a pending deposit request
        val depositsUserA = repository.depositDao.getDepositsByUserId(userA.userId).first()
        val depositsUserB = repository.depositDao.getDepositsByUserId(userB.userId).first()
        assertEquals("User A has 1 pending deposit", 1, depositsUserA.size)
        assertEquals("User B has 0 deposits", 0, depositsUserB.size)
        assertEquals("Deposit status must be Pending", "Pending", depositsUserA[0].status)

        // User B must remain: Plan: None, Deposit: None / 0
        val checkUserBBeforeApproval = repository.userDao.getUserByUserIdOnce(userB.userId)
        assertNull("User B plan must remain null", checkUserBBeforeApproval?.activePlanId)

        // ADMIN approves User A's deposit
        val approveResult = repository.approveDeposit(depositA.depositId)
        assertTrue("Deposit approval must succeed", approveResult.isSuccess)

        // Expected: Only User A is updated
        val updatedUserA = repository.userDao.getUserByUserIdOnce(userA.userId)
        val updatedUserB = repository.userDao.getUserByUserIdOnce(userB.userId)

        // User A has 500 Plan activated with 100 daily return
        assertEquals(500, updatedUserA?.activePlanId)
        assertEquals("Starter 500 Plan", updatedUserA?.activePlanName)
        assertEquals(100.0, updatedUserA?.dailyReturn ?: 0.0, 0.001)
        assertEquals("Active", updatedUserA?.status)

        // User B must remain unchanged: Plan None, Deposit 0!
        assertNull("User B plan must still be null", updatedUserB?.activePlanId)
        assertNull("User B plan name must still be null", updatedUserB?.activePlanName)
        assertEquals("Registered", updatedUserB?.status)
        assertEquals(0.0, updatedUserB?.dailyReturn ?: 0.0, 0.001)

        val updatedDepositsUserB = repository.depositDao.getDepositsByUserId(userB.userId).first()
        assertEquals("User B must still have 0 deposits", 0, updatedDepositsUserB.size)
    }
}
