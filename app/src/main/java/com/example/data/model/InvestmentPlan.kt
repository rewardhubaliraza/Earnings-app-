package com.example.data.model

data class InvestmentPlan(
    val planId: Int,
    val name: String,
    val price: Double,
    val dailyReturn: Double,
    val validityDays: Int = 30,
    val description: String = "Earn Rs. ${dailyReturn.toInt()} daily for $validityDays days. Capital returned on maturity."
) {
    companion object {
        // EXACT configured plans as specified:
        // 500 -> configured daily return 100
        // 1000 -> configured daily return 200
        // 2000 -> configured daily return 400
        // 5000 -> configured daily return 800
        // 10000 -> configured daily return 1500
        val ALL_PLANS = listOf(
            InvestmentPlan(
                planId = 500,
                name = "Starter 500 Plan",
                price = 500.0,
                dailyReturn = 100.0,
                validityDays = 30,
                description = "Daily profit: Rs. 100 | Total profit: Rs. 3,000"
            ),
            InvestmentPlan(
                planId = 1000,
                name = "Basic 1000 Plan",
                price = 1000.0,
                dailyReturn = 200.0,
                validityDays = 30,
                description = "Daily profit: Rs. 200 | Total profit: Rs. 6,000"
            ),
            InvestmentPlan(
                planId = 2000,
                name = "Standard 2000 Plan",
                price = 2000.0,
                dailyReturn = 400.0,
                validityDays = 30,
                description = "Daily profit: Rs. 400 | Total profit: Rs. 12,000"
            ),
            InvestmentPlan(
                planId = 5000,
                name = "Premium 5000 Plan",
                price = 5000.0,
                dailyReturn = 800.0,
                validityDays = 30,
                description = "Daily profit: Rs. 800 | Total profit: Rs. 24,000"
            ),
            InvestmentPlan(
                planId = 10000,
                name = "Ultimate 10000 Plan",
                price = 10000.0,
                dailyReturn = 1500.0,
                validityDays = 30,
                description = "Daily profit: Rs. 1,500 | Total profit: Rs. 45,000"
            )
        )

        fun findById(id: Int): InvestmentPlan? {
            return ALL_PLANS.find { it.planId == id }
        }
    }
}
