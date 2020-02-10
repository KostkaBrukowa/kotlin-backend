package com.example.graphql.resolvers.configuration

import graphql.schema.DataFetcher
import graphql.schema.DataFetcherFactory
import graphql.schema.DataFetcherFactoryEnvironment
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.stereotype.Component

@Component
class SpringDataFetcherFactory : DataFetcherFactory<Any>, BeanFactoryAware {
    private lateinit var beanFactory: BeanFactory

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(environment: DataFetcherFactoryEnvironment?): DataFetcher<Any> {

        val targetedTypeName = environment?.fieldDefinition?.name

        return beanFactory.getBean("${targetedTypeName}DataFetcher".capitalize()) as DataFetcher<Any>
    }
}
