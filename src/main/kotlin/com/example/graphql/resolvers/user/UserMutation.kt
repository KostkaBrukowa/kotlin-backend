package com.example.graphql.resolvers.user

import com.example.graphql.domain.user.User
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class UserMutation : Mutation {
    fun getUser(id: String): Test {
        return Test(id)
    }
}

data class Test(val name: String)
