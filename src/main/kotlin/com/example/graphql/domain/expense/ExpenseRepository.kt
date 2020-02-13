package com.example.graphql.domain.expense

interface ExpenseRepository {
    fun saveNewExpense(newExpense: Expense): Expense

    fun findExpenseById(expenseId: Long): Expense?
    fun findExpenseWithPayments(expenseId: Long): Expense?

    fun findExpensesWithParties(partiesIds: Set<Long>): Set<Expense>
    fun findExpensesWithPayers(partiesIds: Set<Long>): Set<Expense>

    fun updateExpense(updatedExpense: Expense)

    fun removeExpense(expenseToDelete: Expense)
}
