package com.example.graphql.adapters.pgsql.message

import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.resolvers.message.MessageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import javax.transaction.Transactional

interface PersistentBulkPaymentMessageRepository : JpaRepository<PersistentBulkPaymentMessage, Long> {
}

interface PersistentExpenseMessageRepository : JpaRepository<PersistentExpenseMessage, Long> {

}

interface PersistentPartyMessageRepository : JpaRepository<PersistentPartyMessage, Long> {
}

interface PersistentPaymentMessageRepository : JpaRepository<PersistentPaymentMessage, Long> {

}

interface MessageRepository {
    fun saveNewMessage(text: String, userId: Long, entityId: Long): PersistentMessage
}
