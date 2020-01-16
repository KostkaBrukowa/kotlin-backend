package com.example.graphql.resolvers.auth

import com.example.graphql.domain.auth.AuthService
import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class AuthMutation(private val authService: AuthService) : Mutation {

    @GraphQLDescription("Registers user and return JWT token as return value")
    fun signUp(input: UserAuthInput): String? = authService.signUpUser(input.email, input.password)

    @GraphQLDescription("Logs in user and return JWT token as return value")
    fun logIn(input: UserAuthInput): String? = authService.logInUser(input.email, input.password)
}

data class UserAuthInput(val email: String, val password: String)
