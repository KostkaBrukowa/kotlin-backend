package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.user.User

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PaymentTestBuilder {

    private static def defaults = [
            id             : 0,
            amount         : 44.44,
            confirmImageUrl: null,
            status         : PaymentStatus.IN_PROGRESS,
            expense        : null,
            user           : null,
            createdAt      : ZonedDateTime.now(),
            paidAt         : null
    ]


    private PaymentTestBuilder() {}

    static Payment defaultPayment(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new Payment(
                allArgs.id as Long,
                allArgs.amount as Float,
                allArgs.confirmImageUrl as String,
                allArgs.status as PaymentStatus,
                allArgs.createdAt as ZonedDateTime,
                allArgs.paidAt as ZonedDateTime,
                allArgs.expense as Expense,
                allArgs.user as User
        )
    }
}
