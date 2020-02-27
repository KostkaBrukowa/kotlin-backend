package com.example.graphql.domain.message

import com.example.graphql.resolvers.message.MessageType

interface MessageRepository {
    fun findMessageById(messageId: Long, messageType: MessageType): Message?

    fun saveNewMessage(text: String, currentUserId: Long, messageType: MessageType, entityId: Long): Message

    fun removeMessage(messageId: Long, messageType: MessageType)

}
