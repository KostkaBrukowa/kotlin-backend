package com.example.graphql.domain.expense

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.user.User
import java.time.ZonedDateTime

data class Expense(
        val id: Long,
        val amount: String,
        val expenseDate: ZonedDateTime,
        val description: String,


        val user: User,
        val party: Party,
        val payments: List<Payment>
)
