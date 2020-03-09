package com.example.graphql.domain.notification

interface NotificationRepository {

    fun findUserNotifications(userId: Long): List<Notification>
    fun findNotificationsWithUsers(notificationsIds: Iterable<Long>): List<Notification>

    fun sendMessagesNotifications(notifications: Iterable<NewMessageNotification>): List<Notification>
    fun sendExpenseNotifications(notifications: Iterable<NewExpenseNotification>): List<Notification>
    fun sendPartyRequestsNotifications(notifications: Iterable<NewPartyRequestNotification>): List<Notification>
    fun sendPaymentsNotifications(notifications: List<UpdatePaymentStatusNotification>): List<Notification>

    fun markNotificationsAsRead(notificationsIds: Iterable<Long>)
}
