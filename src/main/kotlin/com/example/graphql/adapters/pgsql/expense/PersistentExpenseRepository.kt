package com.example.graphql.adapters.pgsql.expense

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.ExpenseStatus
import com.example.graphql.domain.expense.PersistentExpense
import org.springframework.data.annotation.Persistent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime
import javax.transaction.Transactional

interface PersistentExpenseRepository : JpaRepository<PersistentExpense, Long> {

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentExpense 
        SET amount = :amount, description = :description, expenseDate = :expenseDate, expenseStatus = :status
        WHERE id = :expenseId
    """)
    fun updateExpense(
            @Param("expenseId") expenseId: Long,
            @Param("amount") amount: Float,
            @Param("description") description: String,
            @Param("expenseDate") expenseDate: ZonedDateTime,
            @Param("status") status: ExpenseStatus
    )

    @Query("""
        SELECT expense
        FROM PersistentExpense as expense
        LEFT JOIN FETCH expense.payments
        WHERE expense.id in :expensesIds
    """)
    fun findExpenseWithPayments(@Param("expensesIds") expensesIds: Set<Long>): List<PersistentExpense>
}
