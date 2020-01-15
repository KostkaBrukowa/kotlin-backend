package com.example.graphql

import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.configuration.security.JWTAuthenticationFilter
import com.example.graphql.configuration.security.JWTAuthorizationFilter
import com.example.graphql.configuration.security.SecurityConstants
import com.example.graphql.schema.extensions.CustomSchemaGeneratorHooks
import com.expediagroup.graphql.directives.KotlinDirectiveWiringFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain


@Configuration
@EnableWebSecurity
class WebSecurity(private val bCryptPasswordEncoder: PasswordEncoder) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, SecurityConstants.SIGN_UP_URL).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(JWTAuthenticationFilter(authenticationManager()))
                .addFilter(JWTAuthorizationFilter(authenticationManager())) // this disables session creation on Spring Security
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }


    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.authorizeExchange().pathMatchers(HttpMethod.POST, SecurityConstants.SIGN_UP_URL).permitAll()
        return http.build()
    }
//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", CorsConfiguration().applyPermitDefaultValues())
//        return source
//    }
}

@SpringBootApplication()
class GraphqlApplication(userRepository: PersistentUserRepository) {
    init {
//        userRepository.save(PersistentUser(
//                id = 123,
//                partyRequests = emptyList(),
//                messageGroups = emptyList(),
//                password = "dkfsA,",
//                name = "fjadks",
//                expenses = emptyList(),
//                email = "dfka",
//                bankAccount = null
//        ))
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun wiringFactory() = KotlinDirectiveWiringFactory()

    @Bean
    fun hooks(wiringFactory: KotlinDirectiveWiringFactory) = CustomSchemaGeneratorHooks(wiringFactory)
}

fun main(args: Array<String>) {
    runApplication<GraphqlApplication>(*args)
}
