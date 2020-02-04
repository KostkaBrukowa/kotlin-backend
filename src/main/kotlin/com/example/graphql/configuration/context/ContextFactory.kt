package com.example.graphql.configuration.context

import com.example.graphql.configuration.security.JWTAuthentication
import com.expediagroup.graphql.spring.execution.GraphQLContextFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component

@Component
class MyGraphQLContextFactory(private val jwtAuthentication: JWTAuthentication) : GraphQLContextFactory<AppGraphQLContext> {
    override suspend fun generateContext(
            request: ServerHttpRequest,
            response: ServerHttpResponse
    ): AppGraphQLContext {
        val decodedJWT = jwtAuthentication.handleJWTAuthorisation(request, response)

        return AppGraphQLContext(
                decodedJWT != null,
                decodedJWT?.subject?.toLong() ?: 0,
                request,
                response
        )
    }
}
