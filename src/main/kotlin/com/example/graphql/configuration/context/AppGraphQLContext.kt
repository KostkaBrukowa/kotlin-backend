package com.example.graphql.configuration.context

import com.auth0.jwt.interfaces.DecodedJWT

data class AppGraphQLContext(val authenticated: Boolean, val subject: String?, val decodedJWT: DecodedJWT?)

