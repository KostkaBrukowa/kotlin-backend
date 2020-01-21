package com.example.graphql.configuration.context

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse

data class AppGraphQLContext(
        val authenticated: Boolean,
        val subject: String?,
        val request: ServerHttpRequest,
        val response: ServerHttpResponse
)

