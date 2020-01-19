package com.example.graphql.resolvers.user

import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class UserQuery(private val userService: UserService) : Query {

    @Authenticated(role = Roles.USER)
    fun getUser(id: String): User? {
        return userService.getUserById(id)
    }
}
