package com.example.graphql.domain.message

interface MessageRepository {
    fun saveNewMessage(message: Message): Message
}
