package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.user.User
import com.expediagroup.graphql.annotations.GraphQLID

data class Payment(
        @GraphQLID
        val id: String,
        val expense: Expense,
        val user: User,
        val amount: String?,
        val confirmImageUrl: String?,
        val status: PaymentStatus
)

enum class PaymentStatus {
    ACCEPTED,
    DECLINED,
    IN_PROGRESS,
    PAID,
}

