package com.example.graphql.resolvers.expense

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.expense.ExpenseService
import com.example.graphql.domain.expense.ExpenseStatus
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
class ExpenseQuery(private val expenseService: ExpenseService) : Query {

    @Authenticated(role = Roles.USER)
    fun getSingleExpense(
            expenseId: String,
            @GraphQLContext context: AppGraphQLContext
    ): ExpenseType? {
        return expenseService.findExpenseById(expenseId.toLong(), context.subject)?.toResponse()
    }

    @Authenticated(role = Roles.USER)
    fun getExpensesForUser(
            userId: String,
            @GraphQLContext context: AppGraphQLContext
    ): List<ExpenseType> {
        return expenseService.getExpensesForUser(userId.toLong(), context.subject).map { it.toResponse() }
    }

    @Authenticated(role = Roles.USER)
    fun getExpensesForParty(
            partyId: String,
            @GraphQLContext context: AppGraphQLContext
    ): List<ExpenseType> {
        return expenseService.getExpensesForParty(partyId.toLong(), context.subject).map { it.toResponse() }
    }
}
