package com.example.graphql.resolvers.notification

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class NotificationMutation(private val notificationService: NotificationService) : Mutation {

    @Authenticated(role = Roles.USER)
    fun markNotificationsAsRead(
            notificationsIds: List<String>,
            @GraphQLContext context: AppGraphQLContext
    ) = notificationService.markNotificationsAsRead(notificationsIds.map {it.toLong()}, context.subject).map {it.toResponse()}

    @Authenticated(role = Roles.USER)
    fun removeNotification(
            notificationId: String,
            @GraphQLContext context: AppGraphQLContext
    ) = notificationService.removeNotification(notificationId.toLong(), context.subject).toResponse()
}
