package com.example.graphql.domain.message

import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.user.User
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime

data class Message(
        @GraphQLID
        val id: String,
        val text: String,
        val sendDate: ZonedDateTime,
        val user: User,
        val messageGroup: MessageGroup
)
