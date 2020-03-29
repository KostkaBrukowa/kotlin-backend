package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.domain.payment.BulkPaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import javax.transaction.Transactional

interface PersistentBulkPaymentRepository : JpaRepository<PersistentBulkPayment, Long>, PersistentBulkPaymentRepositoryCustom {
    fun findAllByPayerIdOrReceiverId(payerId: Long, receiverId: Long): List<PersistentBulkPayment>

    @Query("""
        SELECT distinct p
        FROM PersistentBulkPayment as p
        LEFT JOIN FETCH p.messages
        WHERE p.id in (:ids)
    """)
    fun findPaymentsWithMessages(ids: Set<Long>): List<PersistentBulkPayment>

    @Query("""
        SELECT distinct p
        FROM PersistentBulkPayment as p
        LEFT JOIN FETCH p.payer
        JOIN FETCH p.receiver
        WHERE p.id in (:ids)
    """)
    fun findPaymentsWithPayerAndReceiver(ids: Set<Long>): List<PersistentBulkPayment>

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentBulkPayment 
        SET status = :status
        WHERE id = :id
    """)
    fun updateBulkPaymentStatus(@Param("id") id: Long, @Param("status") status: BulkPaymentStatus)

    @Query("""
        SELECT distinct payment
        FROM PersistentBulkPayment as payment
        LEFT JOIN FETCH payment.payer
        WHERE payment.id IN (:ids)
    """)
    fun findPaymentsWithPayers(ids: Set<Long>): List<PersistentBulkPayment>

    @Query("""
        SELECT distinct payment
        FROM PersistentBulkPayment as payment
        LEFT JOIN FETCH payment.receiver
        WHERE payment.id IN (:ids)
    """)
    fun findPaymentsWithReceivers(ids: Set<Long>): List<PersistentBulkPayment>

    @Query("""
        SELECT distinct payment
        FROM PersistentBulkPayment as payment
        LEFT JOIN FETCH payment.payments
        WHERE payment.id IN (:ids)
    """)
    fun findPaymentsWithPayments(ids: Set<Long>): List<PersistentBulkPayment>
}

interface PersistentBulkPaymentRepositoryCustom {

    // TODO ADD A FUNCTIONALITY FOR THIS
    fun addPaymentToBulkPayment(paymentId: Long, paymentOwnerId: Long, expenseOwnerId: Long): Int
}
