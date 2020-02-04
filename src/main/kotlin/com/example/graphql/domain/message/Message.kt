package com.example.graphql.domain.message

import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.user.User
import java.time.ZonedDateTime

data class Message(
        val id: Long,
        val text: String,
        val sendDate: ZonedDateTime,


        val user: User,
        val messageGroup: MessageGroup
)
