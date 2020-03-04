package com.example.graphql.domain.message

import com.example.graphql.domain.user.User
import java.time.ZonedDateTime

data class Message(
        val id: Long = 0,
        val text: String,
        val sendDate: ZonedDateTime = ZonedDateTime.now(),


        val user: User
)
