package com.example.graphql.configuration.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.graphql.domain.user.UserRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class JWTClient(private val userRepository: UserRepository) {
    fun createJWTToken(userId: String): String {
        return createToken(userId, SecurityConstants.JWT_EXPIRATION_TIME)
    }

    fun createRefreshToken(userId: String): String {
        return createToken(userId, SecurityConstants.REFRESH_EXPIRATION_TIME)
    }

    fun validateAndCreateValidationTokens(token: String): ValidationTokensResponse {
        return try {
            val subject = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
                    .build()
                    .verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
                    .subject

            if(userRepository.findUserById(subject.toLong()) == null) throw Exception("Subject not found in database")

            createAuthenticationTokensResponse(subject)
        } catch (e: Exception) {
            throw JWTAuthentication.ClientAuthenticationException("Token was not valid")
        }
    }

    fun createAuthenticationTokensResponse(subject: String): ValidationTokensResponse {
        val jwtToken = createJWTToken(subject)
        val refreshToken = createRefreshToken(subject)

        return ValidationTokensResponse(jwtToken, refreshToken)
    }

    private fun createToken(userId: String, expirationTime: Long): String {
        return JWT.create()
                .withSubject(userId)
                .withExpiresAt(Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
    }

    data class ValidationTokensResponse(val jwtToken: String, val refreshToken: String)
}
