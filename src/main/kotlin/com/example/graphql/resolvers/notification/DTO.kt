package com.example.graphql.resolvers.notification

import com.example.graphql.adapters.pgsql.notification.NotificationEvent
import com.example.graphql.adapters.pgsql.notification.NotificationObjectType
import com.example.graphql.domain.notification.Notification
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime

fun Notification.toResponse(): NotificationType = when (objectType) {
    NotificationObjectType.EXPENSE -> ExpenseNotification(id.toString(), createdAt, event, isRead, objectId)
    NotificationObjectType.PARTY_REQUEST -> PartyRequestNotification(id.toString(), createdAt, event, isRead, objectId)
    NotificationObjectType.PAYMENT -> PaymentNotification(id.toString(), createdAt, event, isRead, objectId)
    NotificationObjectType.BULK_PAYMENT -> PaymentNotification(id.toString(), createdAt, event, isRead, objectId)
    else -> throw UnsupportedResponseNotificationType()
}

interface NotificationType {
    @GraphQLID
    val id: String
    val createdAt: ZonedDateTime
    val event: NotificationEvent
    val isRead: Boolean
    val type: NotificationTypeEnum
}

class PaymentNotification(
        @GraphQLID
        override val id: String,
        override val createdAt: ZonedDateTime,
        override val event: NotificationEvent,
        override val isRead: Boolean,
        val paymentId: Long
) : NotificationType {

    override val type: NotificationTypeEnum
        get() = NotificationTypeEnum.PAYMENT
}

class ExpenseNotification(
        @GraphQLID
        override val id: String,
        override val createdAt: ZonedDateTime,
        override val event: NotificationEvent,
        override val isRead: Boolean,
        val expenseId: Long
) : NotificationType {

    override val type: NotificationTypeEnum
        get() = NotificationTypeEnum.EXPENSE
}

class PartyRequestNotification(
        @GraphQLID
        override val id: String,
        override val createdAt: ZonedDateTime,
        override val event: NotificationEvent,
        override val isRead: Boolean,
        val partyId: Long
) : NotificationType {

    override val type: NotificationTypeEnum
        get() = NotificationTypeEnum.PARTY_REQUEST
}

enum class NotificationTypeEnum {
    PAYMENT,
    EXPENSE,
    PARTY_REQUEST
}

class UnsupportedResponseNotificationType : InternalError("Unsupported notification type for resopnse")

