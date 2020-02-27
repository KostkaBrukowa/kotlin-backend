package com.example.graphql.domain.payment

import com.example.graphql.domain.message.Message

interface PaymentRepository {
    fun findPaymentWithOwnerAndExpenseOwner(paymentId: Long): Payment?
    fun findPaymentsWithOwnerAndExpenseOwner(paymentsIds: List<Long>): List<Payment>

    fun findPaymentsWithExpenses(ids: Set<Long>): List<Payment>
    fun findPaymentsWithUsers(ids: Set<Long>): List<Payment>
    fun findBulkPaymentsWithMessages(ids: Set<Long>): Map<Payment, List<Message>>

    fun getPaymentById(paymentId: Long): Payment?
    fun getPaymentsByUserId(userId: Long): List<Payment>

    fun createPayments(payments: List<Payment>)

    fun changeExpensePaymentsStatuses(expenseId: Long, status: PaymentStatus)
    fun updatePaymentsStatuses(paymentsIds: List<Long>, status: PaymentStatus)
    fun updatePaymentsAmounts(updatedPayments: List<Payment>, amount: Float)
    fun convertPaymentsToBulkPayment(paymentsIds: List<Long>, bulkPaymentId: Long)
}
