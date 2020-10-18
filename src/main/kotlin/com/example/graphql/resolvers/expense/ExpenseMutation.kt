package com.example.graphql.resolvers.expense

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.expense.ExpenseService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
@Component
class ExpenseMutation(private val expenseService: ExpenseService) : Mutation {

    @Authenticated(role = Roles.USER)
    fun createExpense(
            @Valid newExpenseInput: NewExpenseInput,
            @GraphQLContext context: AppGraphQLContext
    ) = expenseService.createExpense(newExpenseInput, context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun updateExpense(
            @Valid updateExpenseInput: UpdateExpenseInput,
            @GraphQLContext context: AppGraphQLContext
    ) = expenseService.updateExpense(updateExpenseInput, context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun removeExpense(
            expenseId: String,
            @GraphQLContext context: AppGraphQLContext
    ) = expenseService.deleteExpense(expenseId.toLong(), context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun changeExpenseStatus(
            updateExpenseStatusInput: UpdateExpenseStatusInput,
            @GraphQLContext context: AppGraphQLContext
    ) = expenseService.updateExpenseStatus(updateExpenseStatusInput, context.subject).toResponse()
}
