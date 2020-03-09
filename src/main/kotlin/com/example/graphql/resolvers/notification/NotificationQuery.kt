package com.example.graphql.resolvers.notification

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class NotificationQuery(private val notificationService: NotificationService) : Query {

    @Authenticated(role = Roles.USER)
    fun findUserNotifications(
            userId: Long,
            @GraphQLContext context: AppGraphQLContext
    ) = notificationService.findUserNotifications(userId, context.subject).map { it.toResponse() }

}
