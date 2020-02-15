package com.example.graphql.resolvers.payment

import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class PaymentQuery(private val paymentService: PaymentService) : Query {

    @Authenticated(role = Roles.USER)
    fun getSinglePayment(paymentId: Long): PaymentType? {
        return paymentService.getPaymentById(paymentId)?.toResponse()
    }

    @Authenticated(role = Roles.USER)
    fun getClientsPayments(userId: Long): List<PaymentType> {
        return paymentService.getUserPayments(userId).map { it.toResponse() }
    }
}
