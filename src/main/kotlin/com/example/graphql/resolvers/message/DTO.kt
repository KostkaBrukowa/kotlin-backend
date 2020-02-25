package com.example.graphql.resolvers.message

import com.example.graphql.domain.message.Message
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID
import org.hibernate.validator.constraints.Length
import java.time.ZonedDateTime

data class MessageResponseType(
        @GraphQLID
        override val id: String = "0",

        val text: String,

        val sendDate: ZonedDateTime
) : GQLResponseType {

    lateinit var messageSender: UserType
}

fun Message.toResponse() = MessageResponseType(
        id = this.id.toString(),
        text = this.text,
        sendDate = this.sendDate
)

data class NewMessageInput(

        @field:Length(min = 1)
        val text: String,

        val entityId: Long,

        val messageType: MessageType
)
