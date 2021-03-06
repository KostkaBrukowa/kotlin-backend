package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.expense.ExpenseStatus
import com.example.graphql.domain.user.PersistentUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime
import javax.transaction.Transactional

interface PersistentUserRepository : JpaRepository<PersistentUser, Long> {
    fun findByEmail(email: String): PersistentUser?

    @Query("""
        SELECT u.id, u.name, u.email, u.bank_account, u.password, u.is_email_confirmed
        FROM party_user as pu
        INNER JOIN users as u
            ON pu.user_id = u.id
        WHERE pu.party_id = :partyId
    """, nativeQuery = true)
    fun findAllPartyParticipants(@Param("partyId") partyId: Long): List<PersistentUser>

    @Query("""
        SELECT distinct user
        FROM PersistentUser as user
        LEFT JOIN FETCH user.partyRequests
        WHERE user.id in :usersIds
    """)
    fun findUsersWithPartyRequests(@Param("usersIds") usersIds: Set<Long>): List<PersistentUser>

    @Query("""
        SELECT user
        FROM PersistentUser as user
        LEFT JOIN FETCH user.friends
        LEFT JOIN FETCH user.friendOf
        WHERE user.email = :userEmail
    """)
    fun findUserByEmailWithFriends(@Param("userEmail") userEmail: String): PersistentUser?

    @Query("""
        SELECT distinct user
        FROM PersistentUser as user
        LEFT JOIN FETCH user.expenses
        WHERE user.id in :usersIds
    """)
    fun findUsersWithExpenses(usersIds: Set<Long>): List<PersistentUser>

    @Query("""
        SELECT distinct user
        FROM PersistentUser as user
        LEFT JOIN FETCH user.payments
        WHERE user.id in :usersIds
    """)
    fun findUsersWithPayments(usersIds: Set<Long>): List<PersistentUser>

    @Query("""
        SELECT distinct user
        FROM PersistentUser as user
        LEFT JOIN FETCH user.joinedParties
        WHERE user.id in :usersIds
    """)
    fun findUsersWithJoinedParties(usersIds: Set<Long>):List<PersistentUser>

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentUser 
        SET name = :name, bankAccount = :bankAccount
        WHERE id = :userId
    """)
    fun updateUser(
            @Param("userId") expenseId: Long,
            @Param("name") amount: String?,
            @Param("bankAccount") description: String?
    )

    @Transactional
    @Modifying
    @Query("""
        INSERT INTO friends (user_id, friend_id)
        VALUES (:userId, :friendId)
    """, nativeQuery = true)
    fun addFriend(userId: Long, friendId: Long)

    @Transactional
    @Modifying
    @Query("""
        DELETE FROM friends 
        WHERE (user_id = :userId AND friend_id = :friendId) OR (user_id = :friendId AND friend_id = :userId)
    """, nativeQuery = true)
    fun removeFriend(userId: Long, friendId: Long)


    @Query("""
        SELECT user
        FROM PersistentUser as user
        LEFT JOIN FETCH user.friends
        LEFT JOIN FETCH user.friendOf
        WHERE user.id = :userId
    """)
    fun findUsersFriends(@Param("userId") userId: Long): PersistentUser
}
