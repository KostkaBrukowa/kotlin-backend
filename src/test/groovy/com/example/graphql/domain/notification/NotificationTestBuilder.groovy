package com.example.graphql.domain.notification

import com.example.graphql.adapters.pgsql.notification.NotificationEvent
import com.example.graphql.adapters.pgsql.notification.NotificationObjectType
import com.example.graphql.domain.user.User

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class NotificationTestBuilder {

    private static def defaults = [
            id        : 0,
            createdAt : "2020-01-27T12:33:39.536632+01:00[Europe/Warsaw]",
            objectId  : 0,
            objectName: 'notification object name',
            objectType: NotificationObjectType.EXPENSE,
            event     : NotificationEvent.PAID,
            isRead    : false,
            actor     : null,
            receiver  : null
    ]

    private UserTestBuilder() {}

    static Notification defaultNotification(Map args = [:]) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new Notification(
                allArgs.id as Long,
                ZonedDateTime.parse(allArgs.createdAt),
                allArgs.objectId as Long,
                allArgs.objectName as String,
                allArgs.objectType as NotificationObjectType,
                allArgs.event as NotificationEvent,
                allArgs.isRead as Boolean,
                allArgs.actor as User,
                allArgs.receiver as User,
        )
    }
}
