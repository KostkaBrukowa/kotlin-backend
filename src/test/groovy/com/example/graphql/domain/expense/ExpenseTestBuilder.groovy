package com.example.graphql.domain.expense

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.user.User

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class ExpenseTestBuilder {

    private static def defaults = [
            id           : 0,
            name         : 'test name',
            amount       : 44.44,
            expenseDate  : ZonedDateTime.now().minusDays(1),
            description  : "test description",
            expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING,
            user         : null,
            party        : null,
            payments     : []
    ]


    private ExpenseTestBuilder() {}

    static Expense defaultExpense(Map args = [:]) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new Expense(
                allArgs.id as Long,
                allArgs.name as String,
                allArgs.amount as Float,
                allArgs.expenseDate as ZonedDateTime,
                allArgs.description as String,
                allArgs.expenseStatus as ExpenseStatus,
                allArgs.user as User,
                allArgs.party as Party,
                allArgs.payments as List<Payment>
        )
    }
}
