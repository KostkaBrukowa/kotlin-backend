package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.user.User

data class Payment(
        val id: Long,
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

