package com.example.graphql.adapters.pgsql.expense

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.ExpenseRepository
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.expense.toPersistentEntity
import org.springframework.stereotype.Component

@Component
class PgSqlExpenseRepository(private val expenseRepository: PersistentExpenseRepository) : ExpenseRepository {

    override fun saveNewExpense(newExpense: Expense): Expense {
        return expenseRepository.save(newExpense.toPersistentEntity()).toDomainWithRelations()
    }
}

private fun PersistentExpense.toDomainWithRelations() = this.toDomain().copy(
        user = this.user?.toDomain(),
        party = this.party?.toDomain()
)
