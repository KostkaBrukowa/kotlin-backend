package com.example.graphql.resolvers.utils

import com.expediagroup.graphql.annotations.GraphQLID

interface GQLResponseType {

    @GraphQLID
    val id: String
}
