//package com.example.graphql.configuration.security
//
//import com.auth0.jwt.JWT
//import com.auth0.jwt.algorithms.Algorithm.HMAC512
//import com.example.graphql.api.UserCredentials
//import com.example.graphql.configuration.security.SecurityConstants.EXPIRATION_TIME
//import com.example.graphql.configuration.security.SecurityConstants.HEADER_STRING
//import com.example.graphql.configuration.security.SecurityConstants.SECRET
//import com.example.graphql.configuration.security.SecurityConstants.TOKEN_PREFIX
//import com.example.graphql.domain.user.PersistentUser
//import com.example.graphql.domain.user.User
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.springframework.security.authentication.AuthenticationManager
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
//import org.springframework.security.core.Authentication
//import org.springframework.security.core.AuthenticationException
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
//import java.io.IOException
//import java.util.*
//import javax.servlet.FilterChain
//import javax.servlet.ServletException
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse
//
//
//class JWTAuthenticationFilter(private val authenticationManager: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {
//    @Throws(AuthenticationException::class)
//    override fun attemptAuthentication(req: HttpServletRequest,
//                                       res: HttpServletResponse): Authentication {
//        return try {
//            val creds: UserCredentials = ObjectMapper()
//                    .readValue(req.getInputStream(), UserCredentials::class.java)
//            authenticationManager.authenticate(
//                    UsernamePasswordAuthenticationToken(
//                            creds.email,
//                            creds.password,
//                            ArrayList())
//            )
//        } catch (e: IOException) {
//            throw RuntimeException(e)
//        }
//    }
//
//    @Throws(IOException::class, ServletException::class)
//    override fun successfulAuthentication(req: HttpServletRequest,
//                                          res: HttpServletResponse,
//                                          chain: FilterChain,
//                                          auth: Authentication) {
//        val token: String = JWT.create()
//                .withSubject((auth.principal as UserCredentials).email)
//                .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .sign(HMAC512(SECRET.toByteArray()))
//
//        res.addHeader(HEADER_STRING, TOKEN_PREFIX.toString() + token)
//    }
//
//}
