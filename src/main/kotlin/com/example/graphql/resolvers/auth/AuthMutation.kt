package com.example.graphql.resolvers.auth

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.configuration.security.JWTClient
import com.example.graphql.domain.auth.AuthService
import com.example.graphql.domain.user.User
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class AuthMutation(private val authService: AuthService) : Mutation {

    @GraphQLDescription("Registers user and return JWT token as return value")
    fun refreshToken(@GraphQLContext context: AppGraphQLContext): String
            = authService.refreshToken(context)

    @GraphQLDescription("Registers user and return JWT token as return value")
    fun signUp(input: UserAuthInput, @GraphQLContext context: AppGraphQLContext): String
            = authService.signUpUser(input.email, input.password, context)

    @GraphQLDescription("Logs in user and return JWT token as return value")
    fun logIn(input: UserAuthInput, @GraphQLContext context: AppGraphQLContext): String?
            = authService.logInUser(input.email, input.password, context)
}

data class UserAuthInput(val email: String, val password: String)
