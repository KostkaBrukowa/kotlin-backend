package com.example.graphql.domain.message

import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.message.MessageType
import java.awt.TrayIcon
import java.time.ZonedDateTime

data class Message(
        val id: Long = 0,
        val text: String,
        val sendDate: ZonedDateTime = ZonedDateTime.now(),


        val user: User
)
