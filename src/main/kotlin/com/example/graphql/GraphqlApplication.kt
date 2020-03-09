package com.example.graphql

import com.example.graphql.resolvers.configuration.CustomDataFetcherFactoryProvider
import com.example.graphql.resolvers.configuration.SpringDataFetcherFactory
import com.example.graphql.schema.directives.CustomDirectiveWiringFactory
import com.example.graphql.schema.exceptions.CustomDataFetcherExceptionHandler
import com.example.graphql.schema.extensions.CustomSchemaGeneratorHooks
import com.expediagroup.graphql.directives.KotlinDirectiveWiringFactory
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.execution.DataFetcherExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
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

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable().authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/graphql").permitAll()
                .pathMatchers(HttpMethod.GET, "/playground").permitAll()
                .pathMatchers(HttpMethod.GET, "/subscriptions").permitAll()
        return http.build()
    }
}

@SpringBootApplication
class GraphqlApplication {

    @Bean
    fun dataFetcherFactoryProvider(springDataFetcherFactory: SpringDataFetcherFactory, objectMapper: ObjectMapper) =
            CustomDataFetcherFactoryProvider(springDataFetcherFactory, objectMapper)

    @Autowired
    fun configureObjectMapper(mapper: ObjectMapper) {
        mapper.findAndRegisterModules()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun wiringFactory() = CustomDirectiveWiringFactory()

    @Bean
    fun hooks(wiringFactory: KotlinDirectiveWiringFactory) = CustomSchemaGeneratorHooks(wiringFactory)

    @Bean
    fun dataFetcherExceptionHandler(): DataFetcherExceptionHandler = CustomDataFetcherExceptionHandler()
}

fun main(args: Array<String>) {
    runApplication<GraphqlApplication>(*args)

}
