package com.example.graphql.resolvers.notification

import com.example.graphql.adapters.pgsql.notification.NotificationEvent
import com.example.graphql.adapters.pgsql.notification.NotificationObjectType
import com.example.graphql.domain.notification.Notification
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime

fun Notification.toResponse(): NotificationType = when (objectType) {
    NotificationObjectType.EXPENSE -> ExpenseNotification(id.toString(), createdAt, event, isRead, actor?.toResponse(), receiver?.toResponse(), objectId)
    NotificationObjectType.PARTY_REQUEST -> PartyRequestNotification(id.toString(), createdAt, event, isRead, actor?.toResponse(), receiver?.toResponse(), objectId)
    NotificationObjectType.PAYMENT -> PaymentNotification(id.toString(), createdAt, event, isRead, actor?.toResponse(), receiver?.toResponse(), objectId)
    NotificationObjectType.BULK_PAYMENT -> PaymentNotification(id.toString(), createdAt, event, isRead, actor?.toResponse(), receiver?.toResponse(), objectId)
    else -> throw UnsupportedResponseNotificationType()
}

interface NotificationType {
    @GraphQLID
    val id: String
    val createdAt: ZonedDateTime
    val event: NotificationEvent
    val isRead: Boolean
    val type: NotificationTypeEnum
    val actor: UserType?
    val receiver: UserType?
}

class PaymentNotification(
        @GraphQLID
        override val id: String,
        override val createdAt: ZonedDateTime,
        override val event: NotificationEvent,
        override val isRead: Boolean,
        override val actor: UserType?,
        override val receiver: UserType?,
        val paymentId: String
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
        override val actor: UserType?,
        override val receiver: UserType?,
        val expenseId: String
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
        override val actor: UserType?,
        override val receiver: UserType?,
        val partyId: String
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

