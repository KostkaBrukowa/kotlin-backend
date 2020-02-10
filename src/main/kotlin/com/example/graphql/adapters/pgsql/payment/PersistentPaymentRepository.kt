package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.domain.payment.PersistentPayment
import org.springframework.data.jpa.repository.JpaRepository

interface PersistentPaymentRepository : JpaRepository<PersistentPayment, Long> {
    fun findAllByExpenseId(expenseId: Long): List<PersistentPayment>
}
