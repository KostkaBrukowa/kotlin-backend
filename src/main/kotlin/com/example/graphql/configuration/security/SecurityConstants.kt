package com.example.graphql.configuration.security

object SecurityConstants {
    const val SECRET = "SecretKeyToGenJWTs"
    const val JWT_EXPIRATION_TIME: Long = 900000  // 15 min
    const val REFRESH_EXPIRATION_TIME: Long = 864000000 // 10 days
    const val TOKEN_PREFIX = "Bearer "
}

