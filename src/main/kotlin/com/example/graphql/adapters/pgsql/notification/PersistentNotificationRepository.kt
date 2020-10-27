package com.example.graphql.adapters.pgsql.notification

import com.example.graphql.domain.party.PersistentParty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import javax.transaction.Transactional

interface PersistentNotificationRepository : JpaRepository<PersistentNotification, Long> {
    fun getTopById(id: Long): PersistentNotification?
    fun findAllByReceiverId(receiverId: Long): List<PersistentNotification>


    @Query("""
        SELECT distinct n
        FROM PersistentNotification as n
        LEFT JOIN FETCH n.receiver
        WHERE n.id IN (:ids)
        ORDER BY n.createdAt DESC 
    """)
    fun findNotificationsWithUsers(ids: Iterable<Long>): List<PersistentNotification>

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentNotification as n
        SET n.isRead = true
        WHERE n.id in (:ids)
    """)
    fun markNotificationsAsRead(ids: Iterable<Long>)

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentNotification as n
        SET n.isDeleted = true
        WHERE n.id in (:ids)
    """)
    fun markNotificationAsDeleted(ids: Iterable<Long>)
}
