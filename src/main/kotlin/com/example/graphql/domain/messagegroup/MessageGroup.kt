package com.example.graphql.domain.messagegroup

import com.example.graphql.domain.message.Message
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User

data class MessageGroup(
        val id: Long,


        val messages: List<Message>,
        val users: List<User>,
        val party: Party?
)
