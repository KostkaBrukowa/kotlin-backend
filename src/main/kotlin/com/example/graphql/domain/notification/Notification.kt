package com.example.graphql.domain.notification

import com.example.graphql.adapters.pgsql.notification.NotificationEvent
import com.example.graphql.adapters.pgsql.notification.NotificationObjectType
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.message.MessageType
import java.time.ZonedDateTime

data class Notification(
        val id: Long = 0,
        val createdAt: ZonedDateTime,
        val objectId: Long,
        val objectName: String? = null,
        val objectType: NotificationObjectType,
        val event: NotificationEvent,
        val isRead: Boolean = false,


        val actor: User? = null,

        val receiver: User? = null
)
