package com.example.graphql.schema.directives

import com.expediagroup.graphql.annotations.GraphQLDirective

@GraphQLDirective(description = "Checks if user posting values has all access right for a query or mutation")
annotation class Authenticated(val role: Roles)

enum class Roles {
    USER
}
