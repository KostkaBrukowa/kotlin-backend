package com.example.graphql.domain.payment

interface BulkPaymentRepository {
    fun findBulkPaymentById(bulkPaymentId: Long): BulkPayment?
    fun findBulkPaymentsByPayerIdOrReceiver(userId: Long): List<BulkPayment>

    fun findPaymentsWithPayers(ids: Set<Long>): List<BulkPayment>
    fun findPaymentsWithReceivers(ids: Set<Long>): List<BulkPayment>
    fun findBulkPaymentsWithPayments(ids: Set<Long>): List<BulkPayment>


    fun updateBulkPaymentStatus(id: Long, status: BulkPaymentStatus)


    fun createBulkPayment(amount: Float, payerId: Long, receiverId: Long): BulkPayment
}
