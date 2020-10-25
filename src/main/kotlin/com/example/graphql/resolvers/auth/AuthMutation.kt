package com.example.graphql.resolvers.auth

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.configuration.security.JWTClient
import com.example.graphql.domain.auth.AuthService
import com.example.graphql.domain.party.PartyKind
import com.example.graphql.domain.user.User
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.spring.operations.Mutation
import org.hibernate.validator.constraints.Length
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.Min
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive

@Component
class AuthMutation(private val authService: AuthService) : Mutation {

    @GraphQLDescription("Registers user and return JWT token as return value")
    fun refreshToken(@GraphQLContext context: AppGraphQLContext): UserAuthResponse
            = authService.refreshToken(context)

    @GraphQLDescription("Registers user and return JWT token as return value")
    fun signUp(input: NewUserInput, @GraphQLContext context: AppGraphQLContext): UserAuthResponse
            = authService.signUpUser(input.email, input.password, input.name, context)

    @GraphQLDescription("Logs in user and return JWT token as return value")
    fun logIn(input: UserAuthInput, @GraphQLContext context: AppGraphQLContext): UserAuthResponse?
            = authService.logInUser(input.email, input.password, context)

    @GraphQLDescription("Removes the cookie from request")
    fun logOut(@GraphQLContext context: AppGraphQLContext)
            = authService.logOut(context)
}

data class UserAuthInput(
        @Email(message = "Email should be valid")
        val email: String,

        @field:Length(min = 3, max = 256)
        val password: String
)

data class NewUserInput(
        @Email(message = "Email should be valid")
        val email: String,

        @field:Length(min = 3, max = 256)
        val password: String,

        @field:Length(min = 3, max = 256)
        val name: String

)

data class UserAuthResponse(val token: String, val userId: String)
