package com.example.graphql.schema.directives

import com.expediagroup.graphql.directives.KotlinDirectiveWiringFactory
import com.expediagroup.graphql.directives.KotlinSchemaDirectiveEnvironment
import com.expediagroup.graphql.directives.KotlinSchemaDirectiveWiring
import graphql.schema.GraphQLDirectiveContainer
import kotlin.reflect.KClass

class CustomDirectiveWiringFactory : KotlinDirectiveWiringFactory() {

    private val caleOnlyDirectiveWiring = SpecificValueOnlySchemaDirectiveWiring()

    override fun getSchemaDirectiveWiring(environment: KotlinSchemaDirectiveEnvironment<GraphQLDirectiveContainer>): KotlinSchemaDirectiveWiring? = when (environment.directive.name) {
        getDirectiveName(Authenticated::class) -> caleOnlyDirectiveWiring
        else -> null
    }
}

internal fun getDirectiveName(kClass: KClass<out Annotation>): String = kClass.simpleName!!.decapitalize()
