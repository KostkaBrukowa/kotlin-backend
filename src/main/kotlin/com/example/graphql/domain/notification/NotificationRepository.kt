package com.example.graphql.domain.notification

import com.example.graphql.domain.message.Message
import com.example.graphql.resolvers.message.MessageType

interface NotificationRepository {

    fun findUserNotifications(userId: Long): List<Notification>
    fun findNotificationsWithUsers(notificationsIds: Iterable<Long>): List<Notification>

    fun sendMessagesNotifications(message: Message, messageType: MessageType, objectId: Long): List<Notification>
    fun sendExpenseNotifications(notifications: Iterable<NewExpenseNotification>): List<Notification>
    fun sendPartyRequestsNotifications(notifications: Iterable<NewPartyRequestNotification>): List<Notification>
    fun sendPaymentsNotifications(notifications: List<UpdatePaymentStatusNotification>): List<Notification>

    fun markNotificationsAsRead(notificationsIds: Iterable<Long>)
}
