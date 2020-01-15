package com.example.graphql.resolvers.user

import com.example.graphql.domain.auth.AuthService
import com.example.graphql.domain.user.UserService
import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class UserMutation(private val userService: UserService, private val authService: AuthService) : Mutation {

    @GraphQLDescription("Registers user and return JWT token as return value")
    fun signUp(input: UserInput): String {
        val id = userService.saveNewUser(input.email, input.password)

        return authService.createToken(id.toString())
    }

    fun logIn(input: UserInput): String? {
        val id = userService.validateUser(input.email, input.password) ?: return null

        return authService.createToken(id)
    }
}

data class UserInput(val email: String, val password: String)
