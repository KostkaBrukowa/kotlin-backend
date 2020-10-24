package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.payment.PersistentPayment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime
import javax.transaction.Transactional

interface PersistentPaymentRepository : JpaRepository<PersistentPayment, Long> {
    fun findAllByExpenseId(expenseId: Long): List<PersistentPayment>
    fun findAllByUserId(userId: Long): List<PersistentPayment>

    @Query("""
        SELECT distinct p
        FROM PersistentPayment as p
        LEFT JOIN FETCH p.messages
        WHERE p.id in (:ids)
    """)
    fun findPaymentsWithMessages(ids: Set<Long>): List<PersistentPayment>

    @Query("""
        SELECT distinct p
        FROM PersistentPayment as p
        LEFT JOIN FETCH p.expense as e
        LEFT JOIN FETCH e.user
        WHERE p.id in (:ids)
    """)
    fun findPaymentsWithExpenseOwner(ids: Set<Long>): List<PersistentPayment>

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentPayment
        SET paymentStatus = :status
        WHERE expense.id = :expenseId
    """)
    fun changeExpensePaymentsStatuses(@Param("expenseId") expenseId: Long, @Param("status") status: PaymentStatus)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE PersistentPayment 
        SET paymentStatus = :status
        WHERE id in :paymentsIds
    """)
    fun updatePaymentStatus(@Param("paymentsIds") paymentsIds: List<Long>, @Param("status") status: PaymentStatus)

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PersistentPayment 
        SET paidAt = :date
        WHERE id in :paymentsIds
    """, nativeQuery = true)
    fun updatePaymentPaidDate(paymentsIds: List<Long>, date: ZonedDateTime)

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentPayment 
        SET amount = :amount
        WHERE id in :updatedPaymentsIds
    """)
    fun updatePaymentsAmounts(@Param("updatedPaymentsIds") updatedPaymentsIds: Iterable<Long>, @Param("amount") amount: Float)

    @Transactional
    @Modifying
    @Query("""
        UPDATE payments
        SET payment_status = 'BULKED', bulked_payment_id = :bulkPaymentId
        WHERE id in :paymentsIds
    """, nativeQuery = true)
    fun convertPaymentsToBulkPayment(paymentsIds: List<Long>, bulkPaymentId: Long)


}
