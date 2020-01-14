package com.example.graphql

import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.schema.extensions.CustomSchemaGeneratorHooks
import com.expediagroup.graphql.directives.KotlinDirectiveWiringFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder


@SpringBootApplication
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
