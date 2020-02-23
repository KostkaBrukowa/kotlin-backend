package com.example.graphql.resolvers.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.payment.BulkPayment
import com.example.graphql.domain.payment.BulkPaymentStatus
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID


data class PaymentType(

        @GraphQLID
        override val id: String,
        val amount: Float?,
        val confirmImageUrl: String?,
        val status: PaymentStatus = PaymentStatus.IN_PROGRESS
) : GQLResponseType {

    lateinit var paymentExpense: Expense

    lateinit var paymentPayer: UserType
}

fun Payment.toResponse() = PaymentType(
        id = this.id.toString(),
        amount = this.amount,
        confirmImageUrl = this.confirmImageUrl,
        status = this.status
)

data class BulkPaymentType(

        @GraphQLID
        override val id: String,
        val amount: Float?,
        val confirmImageUrl: String?,
        val status: BulkPaymentStatus = BulkPaymentStatus.IN_PROGRESS
) : GQLResponseType {

    lateinit var bulkPaymentReceiver: UserType

    lateinit var bulkPaymentPayer: UserType

    lateinit var bulkPaymentPayments: List<PaymentType>
}

fun BulkPayment.toResponse() = BulkPaymentType(
        id = this.id.toString(),
        amount = this.amount,
        confirmImageUrl = this.confirmImageUrl,
        status = this.status
)

data class UpdatePaymentStatusInput(

        val paymentId: Long,
        val status: PaymentStatus
)

data class UpdateBulkPaymentStatusInput(

        val id: Long,
        val status: BulkPaymentStatus
)
