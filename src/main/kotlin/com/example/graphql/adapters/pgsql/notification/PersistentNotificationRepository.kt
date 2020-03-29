package com.example.graphql.adapters.pgsql.notification

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import javax.transaction.Transactional

interface PersistentNotificationRepository : JpaRepository<PersistentNotification, Long> {
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

//    fun sendPartyMessageNotifications(partyId: Long)
//    @Query("""
//        SELECT distinct n
//        FROM PersistentNotification as n
//        LEFT JOIN FETCH n.receiver
//        WHERE n.id IN (:ids)
//        ORDER BY n.createdAt DESC
//    """)
//    fun sendPaymentMessageNotifications(paymentId: Long)
//    @Query("""
//        SELECT distinct n
//        FROM PersistentNotification as n
//        LEFT JOIN FETCH n.receiver
//        WHERE n.id IN (:ids)
//        ORDER BY n.createdAt DESC
//    """)
//    fun sendBulkPaymentMessageNotifications(paymentId: Long)
//    @Query("""
//        SELECT distinct n
//        FROM PersistentNotification as n
//        LEFT JOIN FETCH n.receiver
//        WHERE n.id IN (:ids)
//        ORDER BY n.createdAt DESC
//    """)
//    fun sendExpenseMessageNotifications(expenseId: Long)
}
