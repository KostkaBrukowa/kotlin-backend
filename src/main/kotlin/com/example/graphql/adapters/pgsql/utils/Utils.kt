package com.example.graphql.adapters.pgsql.utils

import org.hibernate.Hibernate
import java.util.*

fun <T> lazyProxy(property: T): T? {
    return if (Hibernate.isInitialized(property)) property else null
}

fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null);
