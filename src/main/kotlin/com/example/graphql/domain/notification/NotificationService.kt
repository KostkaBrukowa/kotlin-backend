package com.example.graphql.domain.notification

import com.example.graphql.adapters.pgsql.notification.NotificationEvent
import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.message.Message
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.resolvers.message.MessageType
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Service

@Service
class NotificationService(private val notificationRepository: NotificationRepository) {

    fun findUserNotifications(userId: Long, currentUserId: Long): List<Notification> {
        if (userId != currentUserId) throw UnauthorisedException()

        return notificationRepository.findUserNotifications(userId)
    }

    fun markNotificationsAsRead(notificationsIds: List<Long>, currentUserId: Long): Boolean {
        val notifications = notificationRepository.findNotificationsWithUsers(notificationsIds)

        requireNotificationsOwner(notifications, currentUserId)

        notificationRepository.markNotificationsAsRead(notificationsIds)

        return true
    }

    fun removeNotification(notificationId: Long, currentUserId: Long): Notification {
        val notification = notificationRepository.findUserNotificationWithUser(notificationId) ?: throw EntityNotFoundException("notification")

        requireNotificationsOwner(listOf(notification), currentUserId)

        notificationRepository.removeNotification(notificationId)

        return notification
    }

    private fun requireNotificationsOwner(notifications: List<Notification>, currentUserId: Long) {
        notifications.forEach {
            it.receiver?.run { if (it.receiver.id != currentUserId) throw UnauthorisedException() }
                    ?: throw InternalError("Notifications were not entirely fetched")
        }
    }

    fun newExpensePaymentsNotification(expense: Expense, payments: List<Payment>) {
        if (expense.user == null || payments.any { it.user == null }) throw InternalError("User was not entirely fetched")

        val notifications = payments.map {
            NewExpenseNotification(actorId = expense.user.id, receiverId = it.user!!.id, expenseId = expense.id, objectName = expense.name)
        }

        notificationRepository.sendExpenseNotifications(notifications)
    }

    fun newMessageNotification(message: Message, messageType: MessageType, objectId: Long) {
        notificationRepository.sendMessagesNotifications(message, messageType, objectId)
    }

    fun newPartyRequestsNotifications(partyRequests: List<PartyRequest>, partyOwner: Long, partyName: String?) {
        if (partyRequests.any { it.user == null }) throw InternalError("Party request was not entirely fetched")

        val notifications = partyRequests.map {
            NewPartyRequestNotification(actorId = partyOwner, receiverId = it.user!!.id, partyRequestId = it.id, objectName = partyName)
        }

        notificationRepository.sendPartyRequestsNotifications(notifications)
    }

    fun updatePaymentsStatusesNotifications(payments: List<Payment>, paymentStatus: PaymentStatus) {
        if (payments.any { it.user == null || it.expense?.user == null }) throw InternalError("Payment was not entirely fetched")

        val paymentNotifications = payments.map {
            UpdatePaymentStatusNotification(
                    actorId = it.user!!.id,
                    receiverId = it.expense!!.user!!.id,
                    paymentId = it.id,
                    notificationEvent = paymentStatus.toNotificationEvent(),
                    objectName = it.expense.name
            )
        }

        notificationRepository.sendPaymentsNotifications(paymentNotifications)
    }
}

open class SendNotificationType(
        val actorId: Long,
        val receiverId: Long,
        val objectName: String? = null,
        open val notificationEvent: NotificationEvent? = null
)

class NewExpenseNotification(
        actorId: Long,
        receiverId: Long,
        objectName: String? = null,
        notificationEvent: NotificationEvent? = null,
        val expenseId: Long
) : SendNotificationType(actorId, receiverId, objectName, notificationEvent)

class NewMessageNotification(
        actorId: Long,
        objectName: String? = null,
        notificationEvent: NotificationEvent? = null,
        receiverId: Long,
        val objectId: Long,
        val messageType: MessageType
) : SendNotificationType(actorId, receiverId, objectName, notificationEvent)

class NewPartyRequestNotification(
        actorId: Long,
        receiverId: Long,
        objectName: String? = null,
        notificationEvent: NotificationEvent? = null,
        val partyRequestId: Long
) : SendNotificationType(actorId, receiverId, objectName, notificationEvent)

class UpdatePaymentStatusNotification(
        actorId: Long,
        receiverId: Long,
        objectName: String? = null,
        override val notificationEvent: NotificationEvent,
        val paymentId: Long
) : SendNotificationType(actorId, receiverId, objectName, notificationEvent)

private fun PaymentStatus.toNotificationEvent(): NotificationEvent = when (this) {
    PaymentStatus.ACCEPTED -> NotificationEvent.ACCEPTED
    PaymentStatus.DECLINED -> NotificationEvent.DECLINED
    PaymentStatus.PAID -> NotificationEvent.PAID
    PaymentStatus.CONFIRMED -> NotificationEvent.CONFIRMED
    PaymentStatus.BULKED -> NotificationEvent.BULKED

    else -> throw UnsupportedNotificationEventException()
}

class UnsupportedNotificationEventException : InternalError("Notification event not supported")
