package com.example.graphql.domain.expense

import com.example.graphql.domain.message.Message
import com.example.graphql.domain.party.Party

interface ExpenseRepository {
    fun saveNewExpense(newExpense: Expense): Expense

    fun findExpenseById(expenseId: Long): Expense?
    fun findExpenseWithPayments(expenseId: Long): Expense?

    fun findExpensesWithParties(partiesIds: Set<Long>): Set<Expense>
    fun findExpensesWithPayers(partiesIds: Set<Long>): Set<Expense>
    fun findExpensesWithPayments(ids: Set<Long>): Set<Expense>
    fun findExpensesWithMessages(ids: Set<Long>): Map<Expense, List<Message>>

    fun updateExpense(updatedExpense: Expense)

    fun removeExpense(expenseToDelete: Expense)
}
