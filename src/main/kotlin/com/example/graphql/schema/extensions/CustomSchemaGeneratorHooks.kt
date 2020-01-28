package com.example.graphql.schema.extensions

import com.expediagroup.graphql.directives.KotlinDirectiveWiringFactory
import com.expediagroup.graphql.hooks.SchemaGeneratorHooks
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlin.reflect.KType

/**
 * Schema generator hook that adds additional scalar types.
 */
class CustomSchemaGeneratorHooks(override val wiringFactory: KotlinDirectiveWiringFactory) : SchemaGeneratorHooks {

    /**
     * Register additional GraphQL scalar types.
     */
    override fun willGenerateGraphQLType(type: KType): GraphQLType? = when (type.classifier) {
        ZonedDateTime::class -> graphqlZonedDateTimeType
        LocalDateTime::class -> graphqlLocalDateTimeType
        else -> null
    }
}

internal val graphqlZonedDateTimeType = GraphQLScalarType.newScalar()
        .name("Date")
        .description("A type representing a formatted java.util.ZonedDateTime")
        .coercing(ZonedDateTimeCoercing)
        .build()

internal val graphqlLocalDateTimeType = GraphQLScalarType.newScalar()
        .name("LocalDate")
        .description("A type representing a formatted java.util.ZonedDateTime")
        .coercing(LocalDateTimeCoercing)
        .build()

private object ZonedDateTimeCoercing : Coercing<ZonedDateTime, String> {
    override fun parseValue(input: Any?): ZonedDateTime = ZonedDateTime.parse(
            serialize(input).trim()
    )

    override fun parseLiteral(input: Any?): ZonedDateTime? {
        val zonedDateTimeString = (input as? StringValue)?.value?.trim()

        return ZonedDateTime.parse(zonedDateTimeString)
    }

    override fun serialize(dataFetcherResult: Any?): String = dataFetcherResult.toString()
}

private object LocalDateTimeCoercing : Coercing<LocalDateTime, String> {
    override fun parseValue(input: Any?): LocalDateTime = LocalDateTime.parse(
            serialize(input)
    )

    override fun parseLiteral(input: Any?): LocalDateTime? {
        val zonedDateTimeString = (input as? StringValue)?.value

        return LocalDateTime.parse(zonedDateTimeString)
    }

    override fun serialize(dataFetcherResult: Any?): String = dataFetcherResult.toString()
}
