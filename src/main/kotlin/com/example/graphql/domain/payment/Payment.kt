package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.user.User

data class Payment(
        val id: Long = 0,
        val amount: Float?,
        val confirmImageUrl: String?,


        val expense: Expense? = null,
        val user: User? = null,
        val status: PaymentStatus = PaymentStatus.IN_PROGRESS
)

enum class PaymentStatus {
    ACCEPTED,
    DECLINED,
    IN_PROGRESS,
    PAID,
    CONFIRMED
}

