package com.example.graphql.configuration.security

object SecurityConstants {
    const val SECRET = "SecretKeyToGenJWTs"
    const val JWT_EXPIRATION_TIME: Long = 864000000 // 10 days
    const val REFRESH_EXPIRATION_TIME: Long = 864000000 // 10 days
    const val TOKEN_PREFIX = "Bearer "
    const val SIGN_UP_URL = "/sign-up"
}

