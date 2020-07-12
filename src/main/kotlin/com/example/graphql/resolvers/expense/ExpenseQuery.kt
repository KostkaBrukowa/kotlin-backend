package com.example.graphql.resolvers.expense

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.expense.ExpenseService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class ExpenseQuery(private val expenseService: ExpenseService) : Query {

    @Authenticated(role = Roles.USER)
    fun getSingleExpense(
            expenseId: String,
            @GraphQLContext context: AppGraphQLContext
    ): ExpenseType? = expenseService.findExpenseById(expenseId, context.subject)?.toResponse()

    @Authenticated(role = Roles.USER)
    fun getExpensesForUser(
            userId: String,
            @GraphQLContext context: AppGraphQLContext
    ): List<ExpenseType> = expenseService.getExpensesForUser(userId, context.subject).map { it.toResponse() }

    @Authenticated(role = Roles.USER)
    fun getExpensesForParty(
            partyId: String,
            @GraphQLContext context: AppGraphQLContext
    ): List<ExpenseType> = expenseService.getExpensesForParty(partyId, context.subject).map { it.toResponse() }
}
