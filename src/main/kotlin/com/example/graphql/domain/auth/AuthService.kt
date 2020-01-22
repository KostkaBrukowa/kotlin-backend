package com.example.graphql.domain.auth

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.configuration.security.JWTAuthentication
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.expediagroup.graphql.annotations.GraphQLContext
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AuthService(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val jwtAuthentication: JWTAuthentication

) {

    fun signUpUser(email: String, password: String, context: AppGraphQLContext): User? {
        val user = User(email = email, password = passwordEncoder.encode(password))
        val savedUser = userRepository.saveUser(user)

        jwtAuthentication.authenticateUser(savedUser.id, context.request, context.response)

        return savedUser
    }

    fun logInUser(email: String, password: String, context: AppGraphQLContext): User? {
        val user = userRepository.getUserByEmail(email) ?: return null
        val passwordMatches = passwordEncoder.matches(password, user.password)

        if (!passwordMatches) {
            return null
        }

        jwtAuthentication.authenticateUser(user.id, context.request, context.response)

        return user
    }
}
