package com.example.graphql.schema.directives

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.schema.exceptions.handlers.UnauthenticatedException
import com.expediagroup.graphql.directives.KotlinFieldDirectiveEnvironment
import com.expediagroup.graphql.directives.KotlinSchemaDirectiveWiring
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition

class AuthenticationSchemaDirectiveWiring() : KotlinSchemaDirectiveWiring {

    @Throws(RuntimeException::class)
    override fun onField(environment: KotlinFieldDirectiveEnvironment): GraphQLFieldDefinition {
        val field = environment.element
        val roleName = environment.directive.getArgument("role")?.value?.toString() ?: ""
        val originalDataFetcher: DataFetcher<Any> = environment.getDataFetcher()

        val authorisationFetcherFetcher = DataFetcher<Any> { dataEnv ->
            when (Roles.values().firstOrNull { it.name == roleName }) {
                Roles.USER -> checkUserAuthentication(dataEnv)
            }

            originalDataFetcher.get(dataEnv)
        }

        environment.setDataFetcher(authorisationFetcherFetcher)

        return field
    }

    private fun checkUserAuthentication(dataEnv: DataFetchingEnvironment) {
        if (!dataEnv.getContext<AppGraphQLContext>().authenticated) {
            throw UnauthenticatedException()
        }
    }
}
