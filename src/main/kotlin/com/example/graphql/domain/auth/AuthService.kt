package com.example.graphql.domain.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.graphql.configuration.security.SecurityConstants
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {
    fun signUpUser(email: String, password: String): String? {
        val encodedPassword = passwordEncoder.encode(password)
        val user = User(email = email, password = encodedPassword)

        return userRepository.saveUser(user)?.toString()
    }

    fun logInUser(email: String, password: String): String? {
        val user = userRepository.getUserByEmail(email) ?: return null
        val passwordMatches = passwordEncoder.matches(password, user.password)

        return if(passwordMatches) user.id else null
    }

    private fun createToken(userId: String): String {
        return JWT.create()
                .withSubject(userId)
                .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
    }

}
