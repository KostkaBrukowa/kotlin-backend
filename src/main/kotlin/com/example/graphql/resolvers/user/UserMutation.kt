package com.example.graphql.resolvers.user

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.user.UserService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class UserMutation(private val userService: UserService) : Mutation {

    @Authenticated(role = Roles.USER)
    fun addFriend(
            userId: Long,
            @GraphQLContext context: AppGraphQLContext
    ): Boolean {
        return userService.addFriend(userId, context.subject)
    }

    @Authenticated(role = Roles.USER)
    fun removeFriend(
            userId: Long,
            @GraphQLContext context: AppGraphQLContext
    ): Boolean {
        userService.removeFriend(userId, context.subject)

        return true
    }
}
