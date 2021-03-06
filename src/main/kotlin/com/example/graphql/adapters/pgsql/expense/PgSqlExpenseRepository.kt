package com.example.graphql.adapters.pgsql.expense

import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.ExpenseRepository
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.expense.toPersistentEntity
import com.example.graphql.domain.message.Message
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class PgSqlExpenseRepository(private val expenseRepository: PersistentExpenseRepository) : ExpenseRepository {

    @Transactional
    override fun saveNewExpense(newExpense: Expense): Expense =
            expenseRepository.save(newExpense.toPersistentEntity()).toDomainWithRelations()

    override fun findExpenseById(expenseId: Long): Expense? =
            expenseRepository.findById(expenseId).toNullable()?.toDomainWithRelations()

    override fun findExpenseWithPayments(expenseId: Long): Expense? {
        val expense = expenseRepository.findExpenseWithPayments(setOf(expenseId)).firstOrNull()

        return expense?.toDomainWithRelations()?.copy(payments = expense.payments.map { it.toDomain() })
    }

    override fun findExpensesWithParties(partiesIds: Set<Long>): Set<Expense> {
        return expenseRepository.findAllById(partiesIds).map { it.toDomainWithRelations() }.toSet()
    }

    override fun findExpensesWithPayers(partiesIds: Set<Long>): Set<Expense> {
        return expenseRepository.findAllById(partiesIds).map { it.toDomainWithRelations() }.toSet()
    }

    override fun findExpensesWithPayments(ids: Set<Long>): Set<Expense> {
        return expenseRepository.findExpenseWithPayments(ids).map { expense ->
            expense.toDomainWithRelations().copy(payments = expense.payments.map { it.toDomain() })
        }.toSet()
    }

    override fun findExpensesWithMessages(ids: Set<Long>): Map<Expense, List<Message>> {
        return expenseRepository
                .findExpensesWithMessages(ids)
                .associateBy({ it.toDomain() }, { it.messages.map { message -> message.toDomain() } })
    }

    override fun updateExpense(updatedExpense: Expense) {
        expenseRepository.save(updatedExpense.toPersistentEntity())
    }

    override fun removeExpense(expenseToDelete: Expense) = expenseRepository.deleteById(expenseToDelete.id)
}

fun PersistentExpense.toDomainWithRelations() = this.toDomain().copy(
        user = this.user?.toDomain(),
        party = this.party?.toDomain()
)
