package com.example.graphql.resolvers.payment

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.payment.BulkPaymentService
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class PaymentMutation(
        private val paymentService: PaymentService,
        private val bulkPaymentService: BulkPaymentService
) : Mutation {

    @Authenticated(role = Roles.USER)
    fun updatePaymentStatus(
            updatePaymentStatusInput: UpdatePaymentStatusInput,
            @GraphQLContext context: AppGraphQLContext
    ): PaymentType {
        return paymentService.updatePaymentStatus(updatePaymentStatusInput, context.subject).toResponse()
    }

    @Authenticated(role = Roles.USER)
    fun updateBulkPaymentStatus(
            updatePaymentStatusInput: UpdateBulkPaymentStatusInput,
            @GraphQLContext context: AppGraphQLContext
    ): BulkPaymentType {
        return bulkPaymentService.updatePaymentStatus(updatePaymentStatusInput, context.subject).toResponse()
    }

    @Authenticated(role = Roles.USER)
    fun bulkPayments(
            paymentsIds: List<String>,
            @GraphQLContext context: AppGraphQLContext
    ): BulkPaymentType? = bulkPaymentService.convertPaymentsToBulkPayment(paymentsIds.map {it.toLong()}, context.subject)?.toResponse()
}
