package com.example.graphql.domain.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.graphql.configuration.security.SecurityConstants
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthService {
    fun createToken(userId: String): String {
        return JWT.create()
                .withSubject(userId)
                .withExpiresAt(Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
    }
}
