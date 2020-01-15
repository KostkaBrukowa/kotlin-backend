package com.example.graphql.configuration.context

import com.example.graphql.configuration.security.JWTAuthentication
import com.expediagroup.graphql.spring.execution.GraphQLContextFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component

data class MyGraphQLContext(val myCustomValue: Boolean, val subject: String?) {}

@Component
class MyGraphQLContextFactory(private val jwtAuthentication: JWTAuthentication) : GraphQLContextFactory<MyGraphQLContext> {
    override suspend fun generateContext(
            request: ServerHttpRequest,
            response: ServerHttpResponse
    ): MyGraphQLContext {
        val decodedJWT = jwtAuthentication.isAuthenticated(request, response)

        return MyGraphQLContext(
                decodedJWT != null,
                decodedJWT?.subject
        )
    }
}
