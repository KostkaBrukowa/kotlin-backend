package com.example.graphql.domain.payment

interface PaymentRepository {
    fun findPaymentWithOwnerAndExpenseOwner(paymentId: Long): Payment?
    fun findPaymentsWithExpenses(ids: Set<Long>): List<Payment>
    fun findPaymentsWithUsers(ids: Set<Long>): List<Payment>
    fun getPaymentById(paymentId: Long): Payment?
    fun getPaymentsByUserId(userId: Long): List<Payment>

    fun createPayments(payments: List<Payment>)

    fun changeExpensePaymentsStatuses(expenseId: Long, status: PaymentStatus)
    fun updatePaymentStatus(paymentId: Long, status: PaymentStatus)
    fun updatePaymentsAmounts(updatedPayments: List<Payment>, amount: Float)
}
