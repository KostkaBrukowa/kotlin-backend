package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.user.User
import java.time.ZonedDateTime
import javax.persistence.Column

data class Payment(
        val id: Long = 0,
        val amount: Float?,
        val confirmImageUrl: String?,
        val status: PaymentStatus = PaymentStatus.IN_PROGRESS,
        val createdAt: ZonedDateTime,
        val paidAt: ZonedDateTime? = null,


        val expense: Expense? = null,
        val user: User? = null
)

enum class PaymentStatus {
    ACCEPTED,
    DECLINED,
    IN_PROGRESS,
    PAID,
    CONFIRMED,
    BULKED
}

