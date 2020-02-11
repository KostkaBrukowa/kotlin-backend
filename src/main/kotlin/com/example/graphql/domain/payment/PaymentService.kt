package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import org.springframework.stereotype.Component

@Component
class PaymentService {
    fun createPaymentsForExpense(newExpense: Expense, participants: Set<Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
