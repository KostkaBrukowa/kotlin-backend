package com.example.graphql.domain.payment

import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.expense.toResponse
import com.example.graphql.resolvers.message.MessageResponseType
import com.example.graphql.resolvers.message.MessageType
import com.example.graphql.resolvers.message.toResponse
import com.example.graphql.resolvers.payment.PaymentType
import com.example.graphql.resolvers.payment.toResponse
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class PaymentDataLoaderService(
        private val paymentRepository: PaymentRepository,
        private val bulkPaymentRepository: BulkPaymentRepository
) {

    fun paymentToUserDataLoaderMap(ids: Set<Long>): Map<Long, UserType> {
        val partyRequests = paymentRepository.findPaymentsWithUsers(ids)

        return partyRequests.associateBy({ it.id }, { it.user!!.toResponse() })
    }

    fun paymentToExpenseDataLoaderMap(ids: Set<Long>): Map<Long, ExpenseType> {
        val partyRequests = paymentRepository.findPaymentsWithExpenses(ids)

        return partyRequests.associateBy({ it.id }, { it.expense!!.toResponse() })
    }

    fun paymentToMessageDataLoaderMap(ids: Set<Long>): Map<Long, List<MessageResponseType>> {
        val messages = paymentRepository.findBulkPaymentsWithMessages(ids)

        return messages.map { it.key.id to it.value.map {message -> message.toResponse() } }.toMap()
    }


    fun bulkPaymentToPayerDataLoaderMap(ids: Set<Long>): Map<Long, UserType> {
        val partyRequests = bulkPaymentRepository.findPaymentsWithPayers(ids)

        return partyRequests.associateBy({ it.id }, { it.payer!!.toResponse() })
    }

    fun bulkPaymentToReceiverDataLoaderMap(ids: Set<Long>): Map<Long, UserType> {
        val partyRequests = bulkPaymentRepository.findPaymentsWithReceivers(ids)

        return partyRequests.associateBy({ it.id }, { it.receiver!!.toResponse() })
    }

    fun bulkPaymentToPaymentsDataLoaderMap(ids: Set<Long>): Map<Long, List<PaymentType>> {
        val users = bulkPaymentRepository.findBulkPaymentsWithPayments(ids)

        return users.associateBy({ it.id }, { it.payments.map { payment -> payment.toResponse() } })
    }

    fun bulkPaymentToMessageDataLoaderMap(ids: Set<Long>): Map<Long, List<MessageResponseType>> {
        val messages = bulkPaymentRepository.findBulkPaymentsWithMessages(ids)

        return messages.map { it.key.id to it.value.map {message -> message.toResponse() } }.toMap()
    }
}
