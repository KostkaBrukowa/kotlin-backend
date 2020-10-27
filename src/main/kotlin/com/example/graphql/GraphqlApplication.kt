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
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@EnableWebSecurity
@Configuration
class WebSecurity() : WebSecurityConfigurerAdapter() {

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http
                .cors().disable()
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/*.html").permitAll()
                .pathMatchers("/static/js/*.js").permitAll()
                .pathMatchers("/static/css/*.css").permitAll()
                .pathMatchers(HttpMethod.POST, "/graphql").permitAll()
                .pathMatchers(HttpMethod.GET, "/").permitAll()
                .pathMatchers(HttpMethod.GET, "/graphql").permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/graphql").permitAll()
                .pathMatchers(HttpMethod.GET, "/playground").permitAll()
                .pathMatchers(HttpMethod.GET, "/subscriptions").permitAll()


        return http.build()
    }
}

@Component
class CorsFilter : WebFilter {
    @Value("\${FRONTEND_BASE_URL}")
    private val frontendBaseUrl: String? = null

    override fun filter(ctx: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        ctx.response.headers.add("Access-Control-Allow-Origin", frontendBaseUrl)
        ctx.response.headers.add("Access-Control-Allow-Credentials", "true")
        ctx.response.headers.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS")
        ctx.response.headers.add("Access-Control-Allow-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range,Authorization")
        return when {
            ctx.request.method == HttpMethod.OPTIONS -> {
                ctx.response.headers.add("Access-Control-Max-Age", "1728000")
                ctx.response.statusCode = HttpStatus.NO_CONTENT
                Mono.empty()
            }
            else -> {
                ctx.response.headers.add("Access-Control-Expose-Headers", "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range,Authorization")
                chain.filter(ctx) ?: Mono.empty()
            }
        }
    }
}

@SpringBootApplication
class GraphqlApplication() {

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
