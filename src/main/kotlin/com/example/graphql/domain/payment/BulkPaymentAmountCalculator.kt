package com.example.graphql.domain.payment
import kotlin.math.absoluteValue

fun calculateBulkPaymentAmount(payments: List<Payment>): BulkPaymentResult {
    val paymentOwnershipInfo = getPaymentsOwnershipInfo(payments)
    val paymentsParticipants = paymentOwnershipInfo.flatMap { listOf(it.paymentOwnerId, it.expenseOwnerId) }.distinct()

    if (paymentsParticipants.size != 2) {
        throw TooPaymentParticipantsError()
    }

    val (firstUserId, secondUserId) = paymentsParticipants
    val (firstUserPaymentsAmount, secondUserPaymentsAmount) =
            calculateRespectiveUserPaymentsAmounts(firstUserId, secondUserId, paymentOwnershipInfo)
    val bulkPaymentAmount = (firstUserPaymentsAmount - secondUserPaymentsAmount).absoluteValue


    return if (firstUserPaymentsAmount > secondUserPaymentsAmount)
        BulkPaymentResult(
                payerId = firstUserId,
                receiverId = secondUserId,
                amount = bulkPaymentAmount
        )
    else
        BulkPaymentResult(
                payerId = secondUserId,
                receiverId = firstUserId,
                amount = bulkPaymentAmount
        )
}

private fun getPaymentsOwnershipInfo(payments: List<Payment>): List<PaymentsMeta> {
    return payments.map {
        if (it.user == null || it.expense?.user == null) throw InternalError("Expense was not entirely fetched")
        if (it.amount == null) throw InvalidPaymentState()


        PaymentsMeta(it.user.id, it.expense.user.id, it.amount.toDouble())
    }
}

private fun calculateRespectiveUserPaymentsAmounts(
        firstUserId: Long,
        secondUserId: Long,
        paymentOwnershipInfo: List<PaymentsMeta>
): Pair<Double, Double> {
    val firstUserPaymentsAmount = paymentOwnershipInfo.sumByDouble {
        if (it.paymentOwnerId == firstUserId) it.amount else 0.0
    }
    val secondUserPaymentsAmount = paymentOwnershipInfo.sumByDouble {
        if (it.paymentOwnerId == secondUserId) it.amount else 0.0
    }

    return Pair(firstUserPaymentsAmount, secondUserPaymentsAmount)
}

data class PaymentsMeta(val paymentOwnerId: Long, val expenseOwnerId: Long, val amount: Double)
data class BulkPaymentResult(val payerId: Long, val receiverId: Long, val amount: Double)
