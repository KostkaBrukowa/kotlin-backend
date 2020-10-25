package com.example.graphql.resolvers.notification

import com.example.graphql.adapters.pgsql.notification.NotificationEvent
import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class NotificationQuery(private val notificationService: NotificationService) : Query {

    @Authenticated(role = Roles.USER)
    fun findUserNotifications(
            userId: String,
            @GraphQLContext context: AppGraphQLContext
    ): List<NotificationType> {
        return notificationService.findUserNotifications(userId.toLong(), context.subject).filter { !it.isDeleted }.map { it.toResponse() }
//        return listOf(
//                PaymentNotification("12", ZonedDateTime.now(), NotificationEvent.ACCEPTED, false, "name", UserType(name = "actor"), UserType(name ="receiver"),  "33"),
//                ExpenseNotification("13", ZonedDateTime.now(), NotificationEvent.CREATION, false,  "name",UserType(name = "actor"), UserType(name = "receiver"),"33"),
//                PartyRequestNotification("15", ZonedDateTime.now(), NotificationEvent.DELETION, false,  "name",UserType(name = "actor"), UserType(name = "receiver"),"33"),
//                PartyRequestNotification("16", ZonedDateTime.now(), NotificationEvent.ACCEPTED, true,  "name",UserType(name = "actor"), UserType(name = "receiver"),"33")
//        )
    }

}
