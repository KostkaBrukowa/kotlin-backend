package com.example.graphql.resolvers.payment

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class PaymentMutation(private val paymentService: PaymentService) : Mutation {

    @Authenticated(role = Roles.USER)
    fun updatePaymentStatus(
            updatePaymentStatusInput: UpdatePaymentStatusInput,
            @GraphQLContext context: AppGraphQLContext
    ): PaymentType {
        return paymentService.updatePaymentStatus(updatePaymentStatusInput, context.subject).toResponse()
    }
}
