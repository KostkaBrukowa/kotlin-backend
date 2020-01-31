package com.example.graphql.adapters.pgsql.utils

import org.hibernate.Hibernate

fun <T> lazyProxy(property: T): T? {
    return if (Hibernate.isInitialized(property)) property else null
}
