package com.example.graphql.adapters.pgsql.expense

import com.example.graphql.domain.expense.PersistentExpense
import org.springframework.data.jpa.repository.JpaRepository

interface PersistentExpenseRepository : JpaRepository<PersistentExpense, Long> {
}
