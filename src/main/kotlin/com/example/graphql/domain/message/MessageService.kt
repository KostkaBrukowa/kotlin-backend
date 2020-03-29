package com.example.graphql.domain.message

import com.example.graphql.domain.expense.ExpenseRepository
import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.payment.BulkPaymentRepository
import com.example.graphql.domain.payment.PaymentRepository
import com.example.graphql.resolvers.message.MessageType
import com.example.graphql.resolvers.message.NewMessageInput
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component
import javax.persistence.EntityNotFoundException

@Component
class MessageService(
        private val messageRepository: MessageRepository,
        private val partyRepository: PartyRepository,
        private val paymentRepository: PaymentRepository,
        private val bulkPaymentRepository: BulkPaymentRepository,
        private val expenseRepository: ExpenseRepository,
        private val notificationService: NotificationService
) {

    fun addMessage(newMessageInput: NewMessageInput, currentUserId: Long): Message {
        val (text, entityId, messageType) = newMessageInput

        when (messageType) {
            MessageType.PARTY -> requirePartyParticipant(entityId, currentUserId)
            MessageType.PAYMENT -> requirePaymentParticipant(entityId, currentUserId)
            MessageType.BULK_PAYMENT -> requireBulkPaymentParticipant(entityId, currentUserId)
            MessageType.EXPENSE -> requireExpenseParticipant(entityId, currentUserId)
        }

        val newMessage = messageRepository.saveNewMessage(text, currentUserId, messageType, entityId)

        notificationService.newMessageNotification(newMessage, messageType, entityId)

        return newMessage
    }


    fun removeMessage(messageId: Long, messageType: MessageType, currentUserId: Long): Boolean {
        val message = messageRepository.findMessageById(messageId, messageType) ?: throw EntityNotFoundException()

        requireMessageOwner(message, currentUserId)

        messageRepository.removeMessage(messageId, messageType)

        return true
    }

    private fun requireMessageOwner(message: Message, currentUserId: Long) {
        if (message.user.id != currentUserId) throw UnauthorisedException()
    }

    private fun requirePartyParticipant(entityId: Long, currentUserId: Long) {
        val party = partyRepository.findPartiesWithParticipants(setOf(entityId)).firstOrNull()
                ?: throw EntityNotFoundException("party")

        if (party.participants.all { it.id != currentUserId }) throw UnauthorisedException()
    }

    private fun requirePaymentParticipant(entityId: Long, currentUserId: Long) {
        val payment = paymentRepository.findPaymentWithOwnerAndExpenseOwner(entityId)
                ?: throw EntityNotFoundException("party")

        if (payment.user == null || payment.expense?.user == null) throw InternalError("payment was not entirely fetched")

        if (payment.user.id != currentUserId && payment.expense.user.id != currentUserId) {
            throw UnauthorisedException()
        }
    }

    private fun requireBulkPaymentParticipant(entityId: Long, currentUserId: Long) {
        val bulkPayment = bulkPaymentRepository.findBulkPaymentById(entityId)
                ?: throw EntityNotFoundException("party")

        if (bulkPayment.payer == null || bulkPayment.receiver == null) throw InternalError("bulk payment was not entirely fetched")

        if (bulkPayment.payer.id != currentUserId && bulkPayment.receiver.id != currentUserId) {
            throw UnauthorisedException()
        }
    }

    private fun requireExpenseParticipant(entityId: Long, currentUserId: Long) {
        val expense = expenseRepository.findExpensesWithPayments(setOf(entityId)).firstOrNull()
                ?: throw EntityNotFoundException("Expense")
        val expensePayments = paymentRepository.findAllByExpenseId(expense.id)

        if (expense.user?.id != currentUserId && expensePayments.all { it.user!!.id != currentUserId }) {
            throw UnauthorisedException()
        }
    }
}
