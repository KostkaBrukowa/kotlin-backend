package com.example.graphql

import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.schema.extensions.CustomSchemaGeneratorHooks
import com.expediagroup.graphql.directives.KotlinDirectiveWiringFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain


@Configuration
@EnableWebSecurity
class WebSecurity(private val bCryptPasswordEncoder: PasswordEncoder) : WebSecurityConfigurerAdapter() {

//    @Throws(Exception::class)
//    override fun configure(http: HttpSecurity) {
//        http.anonymous()
//                .antMatchers(HttpMethod.POST, "/playground").permitAll()
//    }


    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable().authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/graphql").permitAll()
                .pathMatchers(HttpMethod.GET, "/playground").permitAll()
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
    companion object {
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
