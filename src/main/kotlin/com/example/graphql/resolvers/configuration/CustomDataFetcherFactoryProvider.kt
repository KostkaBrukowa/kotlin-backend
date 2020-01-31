package com.example.graphql.resolvers.configuration

import com.expediagroup.graphql.execution.SimpleKotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcherFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class CustomDataFetcherFactoryProvider(
        private val springDataFetcherFactory: SpringDataFetcherFactory,
        objectMapper: ObjectMapper
) : SimpleKotlinDataFetcherFactoryProvider(objectMapper) {

    override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>): DataFetcherFactory<Any> =
            if (kProperty.isLateinit) {
                springDataFetcherFactory
            } else {
                super.propertyDataFetcherFactory(kClass, kProperty)
            }
}
