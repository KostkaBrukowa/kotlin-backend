package com.example.graphql.domain.expense

import com.example.graphql.schema.exceptions.handlers.UnauthorisedException


fun requireExpenseOwner(expense: Expense?, currentUserId: Long) {
    if (expense?.user == null) throw InternalError("Expense was not entirely fetched")
    if (expense.user.id != currentUserId) throw UnauthorisedException()
}
