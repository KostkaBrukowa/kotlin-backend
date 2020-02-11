package com.example.graphql.domain.expense

interface ExpenseRepository {
    fun saveNewExpense(newExpense: Expense): Expense
}
