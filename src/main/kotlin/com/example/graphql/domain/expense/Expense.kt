package com.example.graphql.domain.expense

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.user.User
import java.time.ZonedDateTime

data class Expense(
        val id: Long = 0,
        val name: String,
        val amount: Float,
        val expenseDate: ZonedDateTime,
        val description: String,
        val expenseStatus: ExpenseStatus = ExpenseStatus.IN_PROGRESS_REQUESTING,


        val user: User? = null,
        val party: Party? = null,
        val payments: List<Payment> = emptyList()
)

enum class ExpenseStatus {
    IN_PROGRESS_REQUESTING,
    IN_PROGRESS_PAYING,
    DECLINED,
    RESOLVED
}
