package com.example.graphql.adapters.pgsql.notification

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPaymentRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.message.Message
import com.example.graphql.domain.notification.*
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.resolvers.message.MessageType
import org.springframework.stereotype.Component

@Component
class PgSqlNotificationRepository(
        private val notificationRepository: PersistentNotificationRepository,
        private val partyRepository: PersistentPartyRepository,
        private val paymentRepository: PersistentPaymentRepository,
        private val bulkPaymentRepository: PersistentBulkPaymentRepository,
        private val expenseRepository: PersistentExpenseRepository

) : NotificationRepository {
    override fun findUserNotificationWithUser(notificationId: Long): Notification? {
        return notificationRepository.getTopById(notificationId)?.toDomainWithRelations()
    }

    override fun findUserNotifications(userId: Long): List<Notification> {
        return notificationRepository.findAllByReceiverId(userId).map { it.toDomainWithRelations() }
    }

    override fun findNotificationsWithUsers(notificationsIds: Iterable<Long>): List<Notification> {
        return notificationRepository.findNotificationsWithUsers(notificationsIds).map { it.toDomainWithRelations() }
    }

    override fun sendMessagesNotifications(message: Message, messageType: MessageType, objectId: Long): List<Notification> {
        return when (messageType) {
            MessageType.PARTY -> sendPartyMessageNotifications(message.user.id, objectId)
            MessageType.PAYMENT -> sendPaymentMessageNotifications(message.user.id, objectId)
            MessageType.BULK_PAYMENT -> sendBulkPaymentMessageNotifications(message.user.id, objectId)
            MessageType.EXPENSE -> sendExpenseMessageNotifications(message.user.id, objectId)
        }
    }

    private fun sendExpenseMessageNotifications(authorId: Long, expenseId: Long): List<Notification> {
        val expense = expenseRepository.findById(expenseId).toNullable()
                ?: throw InternalError("Expense was not entirely fetched")
        val payments = paymentRepository.findAllByExpenseId(expense.id)

        val expenseParticipants = payments.map { it.user }
        val notificationReceivers = (expenseParticipants + expense.user).filter { it!!.id != authorId }

        val newNotifications = notificationReceivers.map {
            PersistentNotification(
                    objectId = expense.id,
                    objectName = expense.name,
                    objectType = NotificationObjectType.EXPENSE,
                    event = NotificationEvent.NEW_MESSAGE,
                    actor = PersistentUser(id = authorId),
                    receiver = PersistentUser(id = it!!.id)
            )
        }

        return notificationRepository.saveAll(newNotifications).map { it.toDomainWithRelations() }
    }

    private fun sendBulkPaymentMessageNotifications(authorId: Long, paymentId: Long): List<Notification> {
        val payment = bulkPaymentRepository.findPaymentsWithPayerAndReceiver(setOf(paymentId)).firstOrNull()
                ?: throw InternalError("Bulk payment was not entirely fetched")

        val paymentPayerId = payment.payer?.id ?: throw InternalError("Bulk payment was not entirely fetched")
        val paymentReceiverId = payment.receiver?.id ?: throw InternalError("Bulk payment was not entirely fetched")
        val messageReceiverId = if (authorId == paymentPayerId) paymentReceiverId else paymentPayerId

        val newNotification =
                PersistentNotification(
                        objectId = payment.id,
                        objectType = NotificationObjectType.BULK_PAYMENT,
                        event = NotificationEvent.NEW_MESSAGE,
                        actor = PersistentUser(id = authorId),
                        receiver = PersistentUser(id = messageReceiverId)
                )

        return listOf(notificationRepository.save(newNotification).toDomainWithRelations())
    }

    private fun sendPaymentMessageNotifications(authorId: Long, paymentId: Long): List<Notification> {
        val payment = paymentRepository.findPaymentsWithExpenseOwner(setOf(paymentId)).firstOrNull()
                ?: throw InternalError("Payment was not entirely fetched")

        val paymentPayerId = payment.user?.id ?: throw InternalError("Payment was not entirely fetched")
        val paymentReceiverId = payment.expense?.user?.id ?: throw InternalError("Payment was not entirely fetched")
        val messageReceiverId = if (authorId == paymentPayerId) paymentReceiverId else paymentPayerId

        val newNotification =
                PersistentNotification(
                        objectId = payment.id,
                        objectType = NotificationObjectType.PAYMENT,
                        event = NotificationEvent.NEW_MESSAGE,
                        actor = PersistentUser(id = authorId),
                        receiver = PersistentUser(id = messageReceiverId)
                )

        return listOf(notificationRepository.save(newNotification).toDomainWithRelations())
    }

    private fun sendPartyMessageNotifications(authorId: Long, partyId: Long): List<Notification> {
        val party = partyRepository.findPartiesWithParticipants(setOf(partyId)).firstOrNull()
                ?: throw InternalError("Party for party message not found")

        val partyOwner = party.owner ?: throw InternalError("Party was not entirely fetched")
        val notificationReceivers = (party.participants + partyOwner).filter { it.id != authorId }

        val newNotifications = notificationReceivers.map {
            PersistentNotification(
                    objectId = party.id,
                    objectName = party.name,
                    objectType = NotificationObjectType.PARTY,
                    event = NotificationEvent.NEW_MESSAGE,
                    actor = PersistentUser(id = authorId),
                    receiver = PersistentUser(id = it.id)
            )
        }

        return notificationRepository.saveAll(newNotifications).map { it.toDomainWithRelations() }
    }

    override fun sendExpenseNotifications(notifications: Iterable<NewExpenseNotification>): List<Notification> {
        val newNotifications = notifications.map {
            it.toPersistentEntity(it.expenseId, NotificationObjectType.PAYMENT, NotificationEvent.CREATION)
        }

        return notificationRepository.saveAll(newNotifications).map { it.toDomainWithRelations() }
    }

    override fun sendPartyRequestsNotifications(notifications: Iterable<NewPartyRequestNotification>): List<Notification> {
        val newNotifications = notifications.map {
            it.toPersistentEntity(it.partyRequestId, NotificationObjectType.PARTY_REQUEST, NotificationEvent.CREATION)
        }

        return notificationRepository.saveAll(newNotifications).map { it.toDomainWithRelations() }
    }

    override fun sendPaymentsNotifications(notifications: List<UpdatePaymentStatusNotification>): List<Notification> {
        val newNotifications = notifications.map {
            it.toPersistentEntity(it.paymentId, NotificationObjectType.PARTY_REQUEST, it.notificationEvent)
        }

        return notificationRepository.saveAll(newNotifications).map { it.toDomainWithRelations() }
    }

    override fun markNotificationsAsRead(notificationsIds: Iterable<Long>) {
        notificationRepository.markNotificationsAsRead(notificationsIds)
    }

    override fun removeNotification(notificationId: Long) {
        notificationRepository.markNotificationAsDeleted(listOf(notificationId))
    }
}

private fun SendNotificationType.toPersistentEntity(
        objectId: Long,
        objectType: NotificationObjectType,
        event: NotificationEvent
) = PersistentNotification(
        objectId = objectId,
        objectName = objectName,
        objectType = objectType,
        event = event,
        actor = PersistentUser(id = actorId),
        receiver = PersistentUser(id = receiverId)
)

private fun PersistentNotification.toDomainWithRelations() = this.toDomain().copy(
        actor = actor?.toDomain(),
        receiver = receiver?.toDomain()
)
