package com.example.graphql.configuration.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component

const val ACCESS_TOKEN = "xppctkn"
//const val REFRESH_TOKEN = "xppcreftkn"

@Component
class JWTAuthentication() {

    fun isAuthenticated(request: ServerHttpRequest, response: ServerHttpResponse): DecodedJWT? {
        return try {
            log.debug("Processing '{}' request to resource: {}", request.method, request.uri)

            when {
                request.isAuthorizationTokenPresent() -> isAuthorisedRequestValid(request, response)
//                request.isCookiePresent(REFRESH_TOKEN) -> isUnauthorisedRequestValid(request, response)
                else -> null
            }
        } catch (e: ClientAuthenticationException) {
            null
        }
    }

    private fun isAuthorisedRequestValid(request: ServerHttpRequest, response: ServerHttpResponse): DecodedJWT {
        val token = request.extractAuthorizationToken()
        log.debug("Handling request with oauth token set.")

        val decodedJWT = decodeTokenSafely(token)
        log.debug("Successfully decoded token.")

        return decodedJWT
    }

//    private fun isUnauthorisedRequestValid(request: ServerHttpRequest, response: ServerHttpResponse): DecodedJWT {
//        log.debug("Handling request with no/expired token. Refresh token cookie is present.")
//
//        // TODO CREATE WITH REFRESH TOKEN
//        val tokenResponse = jwtClient.createTokenUsingRefreshToken(request.getCookieValue(REFRESH_TOKEN))
//        log.debug("Received new OAuth2 token from authorization server using refresh token.")
//
//        response.addOAuthTokensCookies(tokenResponse.token, tokenResponse.refreshToken)
//        return AuthenticatedHttpServletRequestWrapper(request, tokenResponse.token)
//    }

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
            isCookiePresent(ACCESS_TOKEN) -> {
                token = getCookieValue(ACCESS_TOKEN)
                log.debug("Extracted authorization token from cookie.")
            }
            else -> {
                throw IllegalStateException("Authorization header or token cookie should be present but they are not.")
            }
        }

        return token
    }

//    private fun ServerHttpResponse.addOAuthTokensCookies(token: String, refreshToken: String? = null) {
//        this.addCookie(
//                ResponseCookie.from(ACCESS_TOKEN, token).build()
//        )
//
//        refreshToken?.let {
//            this.addCookie(ResponseCookie.from(REFRESH_TOKEN, refreshToken).apply {
//                this.httpOnly(true)
//            }.build()
//            )
//        }
//    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(JWTAuthentication::class.java)
    }
}

