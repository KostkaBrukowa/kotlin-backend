package com.example.graphql.resolvers.user

import com.example.graphql.configuration.context.MyGraphQLContext
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserService
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class UserQuery(private val userService: UserService) : Query {

    fun getUser(id: String): User? = userService.getUserById(id)
}

//data class User(val id: String)

