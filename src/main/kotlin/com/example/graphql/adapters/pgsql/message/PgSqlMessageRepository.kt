package com.example.graphql.adapters.pgsql.message

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPaymentRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.message.Message
import com.example.graphql.domain.message.MessageRepository
import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.resolvers.message.MessageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class PgSqlMessageRepository(
        private val partyMessageRepository: PersistentPartyMessageRepository,
        private val paymentMessageRepository: PersistentPaymentMessageRepository,
        private val bulkPaymentMessageRepository: PersistentBulkPaymentMessageRepository,
        private val expenseMessageRepository: PersistentExpenseMessageRepository,
        private val partyRepository: PersistentPartyRepository,
        private val paymentRepository: PersistentPaymentRepository,
        private val bulkPaymentRepository: PersistentBulkPaymentRepository,
        private val expenseRepository: PersistentExpenseRepository,
        private val userRepository: PersistentUserRepository
) : MessageRepository {
    override fun findMessageById(messageId: Long, messageType: MessageType): Message? {
        val repository = getMessageRepository(messageType)
        val message = repository.findById(messageId)

        return message.toNullable()?.toDomainWithRelations()
    }

    override fun saveNewMessage(text: String, currentUserId: Long, messageType: MessageType, entityId: Long): Message {
        val savedMessage = when (messageType) {
            MessageType.PARTY -> {
                partyMessageRepository.save(PersistentPartyMessage(partyRepository.getOne(entityId)).apply {
                    this.text = text
                    this.user = userRepository.getOne(currentUserId)
                })
            }
            MessageType.PAYMENT -> {
                paymentMessageRepository.save(PersistentPaymentMessage(paymentRepository.getOne(entityId)).apply {
                    this.text = text
                    this.user = userRepository.getOne(currentUserId)
                })
            }
            MessageType.BULK_PAYMENT -> {
                bulkPaymentMessageRepository.save(PersistentBulkPaymentMessage(bulkPaymentRepository.getOne(entityId)).apply {
                    this.text = text
                    this.user = userRepository.getOne(currentUserId)
                })
            }
            MessageType.EXPENSE -> {
                expenseMessageRepository.save(PersistentExpenseMessage(expenseRepository.getOne(entityId)).apply {
                    this.text = text
                    this.user = userRepository.getOne(currentUserId)
                })
            }
        }

        return savedMessage.toDomainWithRelations()
    }

    override fun removeMessage(messageId: Long, messageType: MessageType) {
        val repository = getMessageRepository(messageType)

        repository.deleteById(messageId)
    }

    private fun getMessageRepository(messageType: MessageType): JpaRepository<out PersistentMessage, Long> {
        return when (messageType) {
            MessageType.PARTY -> partyMessageRepository
            MessageType.PAYMENT -> paymentMessageRepository
            MessageType.BULK_PAYMENT -> bulkPaymentMessageRepository
            MessageType.EXPENSE -> expenseMessageRepository
        }
    }
}

private fun PersistentMessage.toDomainWithRelations() = this.toDomain().copy(
        user = this.user?.toDomain()
)

