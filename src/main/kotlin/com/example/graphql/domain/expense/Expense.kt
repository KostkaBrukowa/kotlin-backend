package com.example.graphql.domain.expense

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.user.User
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime

data class Expense(
        @GraphQLID
        val id: String,
        val user: User,
        val payments: List<Payment>,
        val party: Party,
        val amount: String,
        val expenseDate: ZonedDateTime,
        val description: String
)
