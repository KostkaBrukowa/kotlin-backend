package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.user.User

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class BulkBulkPaymentTestBuilder {

    private static def defaults = [
            id             : 0,
            amount         : 44.44,
            confirmImageUrl: null,
            status         : BulkPaymentStatus.IN_PROGRESS,
            payer          : null,
            receiver       : null,
            payments       : []

    ]

    private BulkBulkPaymentTestBuilder() {}

    static BulkPayment defaultBulkPayment(Map args = [:]) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new BulkPayment(
                allArgs.id as Long,
                allArgs.amount as Float,
                allArgs.confirmImageUrl as String,
                allArgs.status as BulkPaymentStatus,
                allArgs.payer as User,
                allArgs.receiver as User,
                allArgs.payments as Set<Payment>
        )
    }
}
