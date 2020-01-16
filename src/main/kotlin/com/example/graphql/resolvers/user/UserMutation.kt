package com.example.graphql.resolvers.user

import com.example.graphql.domain.auth.AuthService
import com.example.graphql.domain.user.UserService
import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class UserMutation(private val userService: UserService, private val authService: AuthService) : Mutation {
}

data class UserInput(val email: String, val password: String)
