package com.example.graphql.domain.expense

import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@ExperimentalContracts
fun requireExpenseOwner(expense: Expense?, currentUserId: Long) {
    contract {
        returns() implies (expense != null)
    }

    if (expense?.user == null) throw InternalError("Expense was not entirely fetched")
    if (expense.user.id != currentUserId) throw UnauthorisedException()
}
