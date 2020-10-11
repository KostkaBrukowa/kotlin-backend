package com.example.graphql.domain.auth

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.configuration.security.JWTAuthentication
import com.example.graphql.configuration.security.REFRESH_TOKEN
import com.example.graphql.configuration.security.removeCookie
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.resolvers.auth.UserAuthResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AuthService(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val jwtAuthentication: JWTAuthentication
) {

    fun signUpUser(email: String, password: String, context: AppGraphQLContext): UserAuthResponse {
        val user = User(email = email, password = passwordEncoder.encode(password))
        val savedUser = try {
            userRepository.saveUser(user)
        } catch (ex: Throwable) {
            throw UserAlreadyExistsException()
        }

        return jwtAuthentication.authenticateUser(savedUser.id.toString(), context.request, context.response).toResponse()
    }

    fun refreshToken(context: AppGraphQLContext): UserAuthResponse {
//        return jwtAuthentication.handleRefreshToken(context.request, context.response).toResponse().token
        val savedUser = userRepository.findUserByEmail("admin@gmail.com")
        return jwtAuthentication.authenticateUser(savedUser?.id.toString(), context.request, context.response).toResponse()
    }

    fun logInUser(email: String, password: String, context: AppGraphQLContext): UserAuthResponse? {
        val user = userRepository.findUserByEmail(email) ?: return null
        val passwordMatches = passwordEncoder.matches(password, user.password)

        if (!passwordMatches) {
            return null
        }

        return jwtAuthentication.authenticateUser(user.id.toString(), context.request, context.response).toResponse()
    }

//    private fun toResponse(token: String): UserAuthResponse {
//        val decodedJWT = jwtAuthentication.decodeTokenSafely(token)
//
//        return UserAuthResponse(token, decodedJWT.subject)
//    }

    private fun String.toResponse(): UserAuthResponse {
        val decodedJWT = jwtAuthentication.decodeTokenSafely(this)

        return UserAuthResponse(this, decodedJWT.subject)
    }

    fun logOut(context: AppGraphQLContext): Boolean {
        context.response.removeCookie(REFRESH_TOKEN)

        return true
    }
}

class UserAlreadyExistsException() : RuntimeException("Użytkownik o podanej nazwie już istnieje")
