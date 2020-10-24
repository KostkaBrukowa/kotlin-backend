package com.example.graphql.resolvers.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.payment.BulkPayment
import com.example.graphql.domain.payment.BulkPaymentStatus
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.message.MessageResponseType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime


data class PaymentType(

        @GraphQLID
        override val id: String,
        val amount: Float?,
        val createdAt: ZonedDateTime,
        val paidAt: ZonedDateTime?,
        val confirmImageUrl: String?,
        val status: PaymentStatus = PaymentStatus.IN_PROGRESS
) : GQLResponseType {

    lateinit var paymentExpense: ExpenseType

    lateinit var paymentPayer: UserType

    lateinit var paymentMessages: List<MessageResponseType>
}

fun Payment.toResponse() = PaymentType(
        id = this.id.toString(),
        amount = this.amount,
        confirmImageUrl = this.confirmImageUrl,
        status = this.status,
        paidAt = this.paidAt,
        createdAt = this.createdAt
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

    lateinit var bulkPaymentMessages: List<MessageResponseType>
}

fun BulkPayment.toResponse() = BulkPaymentType(
        id = this.id.toString(),
        amount = this.amount,
        confirmImageUrl = this.confirmImageUrl,
        status = this.status
)

data class UpdatePaymentStatusInput(

        val paymentId: String,
        val status: PaymentStatus
)

data class UpdateBulkPaymentStatusInput(

        val id: String,
        val status: BulkPaymentStatus
)
