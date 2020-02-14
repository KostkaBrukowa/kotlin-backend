package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.payment.PersistentPayment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import javax.transaction.Transactional

interface PersistentPaymentRepository : JpaRepository<PersistentPayment, Long> {
    fun findAllByExpenseId(expenseId: Long): List<PersistentPayment>
    fun findAllByUserId(userId: Long): List<PersistentPayment>


    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentPayment
        SET paymentStatus = :status
        WHERE expense.id = :expenseId
    """)
    fun changeExpensePaymentsStatuses(@Param("expenseId") expenseId: Long, @Param("status") status: PaymentStatus)

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentPayment 
        SET paymentStatus = :status
        WHERE id = :paymentId
    """)
    fun updatePaymentStatus(@Param("paymentId") paymentId: Long, @Param("status") status: PaymentStatus)
}
