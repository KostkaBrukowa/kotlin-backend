package com.example.graphql.domain.expense

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.user.User
import java.time.ZonedDateTime

data class Expense(
        val id: Long = 0,
        val amount: Float,
        val expenseDate: ZonedDateTime,
        val description: String,


        val user: User? = null,
        val party: Party? = null,
        val payments: List<Payment> = emptyList()
)
