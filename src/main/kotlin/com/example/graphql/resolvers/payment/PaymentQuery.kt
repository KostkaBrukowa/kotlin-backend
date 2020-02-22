package com.example.graphql.resolvers.payment

import com.example.graphql.domain.payment.BulkPaymentService
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class PaymentQuery(
        private val paymentService: PaymentService,
        private val bulkPaymentService: BulkPaymentService
) : Query {

    @Authenticated(role = Roles.USER)
    fun getSinglePayment(paymentId: Long): PaymentType? {
        return paymentService.getPaymentById(paymentId)?.toResponse()
    }

    @Authenticated(role = Roles.USER)
    fun getClientsPayments(userId: Long): List<PaymentType> {
        return paymentService.getUserPayments(userId).map { it.toResponse() }
    }

    @Authenticated(role = Roles.USER)
    fun getClientBulkPayments(userId: Long): List<BulkPaymentType> {
        return bulkPaymentService.getUserBulkPayments(userId).map { it.toResponse() }
    }
}
