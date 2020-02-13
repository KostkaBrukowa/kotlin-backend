package com.example.graphql.adapters.pgsql.expense

import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.ExpenseRepository
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.expense.toPersistentEntity
import org.springframework.stereotype.Component

@Component
class PgSqlExpenseRepository(private val expenseRepository: PersistentExpenseRepository) : ExpenseRepository {

    override fun saveNewExpense(newExpense: Expense): Expense =
            expenseRepository.save(newExpense.toPersistentEntity()).toDomainWithRelations()

    override fun findExpenseById(expenseId: Long): Expense? =
            expenseRepository.findById(expenseId).toNullable()?.toDomainWithRelations()

    override fun findExpenseWithPayments(expenseId: Long): Expense? {
        val expense = expenseRepository.findExpenseWithPayments(expenseId)

        return expense?.toDomainWithRelations()?.copy(payments = expense.payments.map { it.toDomain() })
    }

    override fun findExpensesWithParties(partiesIds: Set<Long>): Set<Expense> {
        return expenseRepository.findAllById(partiesIds).map { it.toDomainWithRelations() }.toSet()
    }

    override fun findExpensesWithPayers(partiesIds: Set<Long>): Set<Expense> {
        return expenseRepository.findAllById(partiesIds).map { it.toDomainWithRelations() }.toSet()
    }

    override fun updateExpense(updatedExpense: Expense) {
        return expenseRepository.updateExpense(
                updatedExpense.id,
                updatedExpense.amount,
                updatedExpense.description,
                updatedExpense.expenseDate,
                updatedExpense.expenseStatus
        )
    }

    override fun removeExpense(expenseToDelete: Expense) = expenseRepository.deleteById(expenseToDelete.id)
}

private fun PersistentExpense.toDomainWithRelations() = this.toDomain().copy(
        user = this.user?.toDomain(),
        party = this.party?.toDomain()
)
