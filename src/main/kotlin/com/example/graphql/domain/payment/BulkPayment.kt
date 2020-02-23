package com.example.graphql.domain.payment

import com.example.graphql.domain.user.User

data class BulkPayment(

        val id: Long = 0,
        val amount: Float?,
        val confirmImageUrl: String?,
        val status: BulkPaymentStatus = BulkPaymentStatus.IN_PROGRESS,


        val payer: User? = null,
        val receiver: User? = null,
        val payments: Set<Payment> = emptySet()
)

enum class BulkPaymentStatus {
    IN_PROGRESS,
    PAID,
    CONFIRMED
}

