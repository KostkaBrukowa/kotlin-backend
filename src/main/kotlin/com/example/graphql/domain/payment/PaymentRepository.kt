package com.example.graphql.domain.payment

interface PaymentRepository {
    fun findPaymentWithOwnerAndExpenseOwner(paymentId: Long): Payment?
    fun findPaymentsWithOwnerAndExpenseOwner(paymentsIds: List<Long>): List<Payment>
    fun findPaymentsWithExpenses(ids: Set<Long>): List<Payment>
    fun findPaymentsWithUsers(ids: Set<Long>): List<Payment>
    fun getPaymentById(paymentId: Long): Payment?
    fun getPaymentsByUserId(userId: Long): List<Payment>

    fun createPayments(payments: List<Payment>)

    fun changeExpensePaymentsStatuses(expenseId: Long, status: PaymentStatus)
    fun updatePaymentsStatuses(paymentId: List<Long>, status: PaymentStatus)
    fun updatePaymentsAmounts(updatedPayments: List<Payment>, amount: Float)
    fun convertPaymentsToBulkPayment(paymentsIds: List<Long>, bulkPaymentId: Long)
}
