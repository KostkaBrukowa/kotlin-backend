package com.example.graphql.resolvers.message

import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class MessageMutation: Mutation {
}

enum class MessageType {
    PARTY,
    PAYMENT,
    BULK_PAYMENT,
    EXPENSE
}
