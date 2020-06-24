package com.example.graphql.configuration.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.graphql.configuration.security.SecurityConstants.JWT_EXPIRATION_TIME
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import java.util.*

const val ACCESS_TOKEN = "xppctkn"
const val REFRESH_TOKEN = "xppcreftkn"

@Component
class JWTAuthentication(private val jwtClient: JWTClient) {

    fun handleJWTAuthorisation(request: ServerHttpRequest, response: ServerHttpResponse): DecodedJWT? {
        return try {
            log.debug("Processing '{}' request to resource: {}", request.method, request.uri)

            when {
                request.isAuthorizationTokenPresent() -> handleJWTValidation(request, response)
                else -> null
            }
        } catch (e: ClientAuthenticationException) {
            null
        }
    }

    private fun handleJWTValidation(request: ServerHttpRequest, response: ServerHttpResponse): DecodedJWT {
        val token = request.extractAuthorizationToken()
        log.debug("Handling request with oauth token set.")

        val decodedJWT = decodeTokenSafely(token)
        log.debug("Successfully decoded token.")

        return when {
            decodedJWT.isNotExpired() -> decodedJWT
            request.isCookiePresent(REFRESH_TOKEN) -> handleRefreshToken(request, response)
            else -> decodedJWT
        }
    }

    fun handleRefreshToken(request: ServerHttpRequest, response: ServerHttpResponse): DecodedJWT {
        log.debug("Handling request with no/expired token. Refresh token cookie is present.")

        val tokenResponse = jwtClient.validateAndCreateValidationTokens(request.getCookieValue(REFRESH_TOKEN))
        log.debug("Received new OAuth2 token from authorization server using refresh token.")

        response.addRefreshTokenCookie(tokenResponse.refreshToken)

        return decodeTokenSafely(tokenResponse.jwtToken)
    }

    fun authenticateUser(subject: String, request: ServerHttpRequest, response: ServerHttpResponse): String {
        val tokenResponse = jwtClient.createAuthenticationTokensResponse(subject)

        response.addRefreshTokenCookie(tokenResponse.refreshToken)

        return tokenResponse.jwtToken
    }

    private fun decodeTokenSafely(token: String): DecodedJWT {
        try {
            return JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.toByteArray()))
                    .build()
                    .verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
        } catch (e: Exception) {
            throw ClientAuthenticationException(cause = e.toString())
        }
    }

    class ClientAuthenticationException(cause: String) : Exception(cause) {}

    private fun ServerHttpRequest.isAuthorizationTokenPresent(): Boolean = isHeaderPresent(HttpHeaders.AUTHORIZATION)

    private fun ServerHttpRequest.extractAuthorizationToken(): String {
        val token: String

        // Authorization header has greater priority
        when {
            isHeaderPresent(HttpHeaders.AUTHORIZATION) -> {
                token = getHeaderValue(HttpHeaders.AUTHORIZATION).replace("Bearer ", "")
                log.debug("Extracted authorization token from header.")
            }
            else -> {
                throw IllegalStateException("Authorization header or token cookie should be present but they are not.")
            }
        }

        return token
    }

    private fun ServerHttpResponse.addRefreshTokenCookie(refreshToken: String? = null) {
        refreshToken?.let {
            this.addCookie(
                    ResponseCookie.from(REFRESH_TOKEN, refreshToken).apply {
                        this.httpOnly(true)
                    }.build()
            )
        }
    }

    private fun DecodedJWT.isNotExpired(): Boolean =
            Calendar.getInstance().time.time + JWT_EXPIRATION_TIME < expiresAt.time

    companion object {
        val log: Logger = LoggerFactory.getLogger(JWTAuthentication::class.java)
    }
}

