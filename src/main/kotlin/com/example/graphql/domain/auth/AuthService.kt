package com.example.graphql.domain.auth

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.configuration.security.JWTAuthentication
import com.example.graphql.configuration.security.JWTClient
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AuthService(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val jwtAuthentication: JWTAuthentication

) {

    fun signUpUser(email: String, password: String, context: AppGraphQLContext): String {
        val user = User(email = email, password = passwordEncoder.encode(password))
        val savedUser = userRepository.saveUser(user)

        return jwtAuthentication.authenticateUser(savedUser.id.toString(), context.request, context.response)
    }

    fun refreshToken(context: AppGraphQLContext): String {
        return jwtAuthentication.handleRefreshToken(context.request, context.response).token
    }

    fun logInUser(email: String, password: String, context: AppGraphQLContext): String?{
        val user = userRepository.findUserByEmail(email) ?: return null
        val passwordMatches = passwordEncoder.matches(password, user.password)

        if (!passwordMatches) {
            return null
        }

        return jwtAuthentication.authenticateUser(user.id.toString(), context.request, context.response)
    }
}
