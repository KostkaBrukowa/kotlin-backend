package com.example.graphql.adapters.pgsql.notification

import com.example.graphql.domain.notification.*
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.resolvers.message.MessageType
import org.springframework.stereotype.Component

@Component
class PgSqlNotificationRepository(private val notificationRepository: PersistentNotificationRepository) : NotificationRepository {
    override fun findUserNotifications(userId: Long): List<Notification> {
        return notificationRepository.findAllByReceiverId(userId).map { it.toDomainWithRelations() }
    }

    override fun findNotificationsWithUsers(notificationsIds: Iterable<Long>): List<Notification> {
        return notificationRepository.findNotificationsWithUsers(notificationsIds).map { it.toDomainWithRelations() }
    }

    override fun sendMessagesNotifications(notifications: Iterable<NewMessageNotification>): List<Notification> {
        val newNotifications = notifications.map {
            it.toPersistentEntity(it.objectId, it.messageType.toNotificationObjectType(), NotificationEvent.NEW_MESSAGE)
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

private fun MessageType.toNotificationObjectType() = when (this) {
    MessageType.PARTY -> NotificationObjectType.PARTY
    MessageType.PAYMENT -> NotificationObjectType.PAYMENT
    MessageType.BULK_PAYMENT -> NotificationObjectType.BULK_PAYMENT
    MessageType.EXPENSE -> NotificationObjectType.EXPENSE
}

