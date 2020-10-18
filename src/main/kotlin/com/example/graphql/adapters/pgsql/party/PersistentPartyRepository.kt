package com.example.graphql.adapters.pgsql.party

import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.user.PersistentUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import javax.transaction.Transactional

interface PersistentPartyRepository : JpaRepository<PersistentParty, Long> {
    fun getAllByOwnerId(id: Long): List<PersistentParty>
    fun getTopById(id: Long): PersistentParty?

    @Query("""
        SELECT distinct p
        FROM PersistentParty as p
        LEFT JOIN FETCH p.partyRequests
        WHERE p.id IN (:ids)
    """)
    fun findPartiesWithPartyRequests(@Param("ids") ids: Set<Long>): List<PersistentParty>

    @Query("""
        SELECT distinct p
        FROM PersistentParty as p
        LEFT JOIN FETCH p.participants
        WHERE p.id in (:ids)
    """)
    fun findPartiesWithParticipants(@Param("ids") ids: Iterable<Long>): List<PersistentParty>

    @Query("""
        SELECT distinct p
        FROM PersistentParty as p
        LEFT JOIN FETCH p.messages
        WHERE p.id in (:ids)
    """)
    fun findPartiesWithMessages(ids: Set<Long>): List<PersistentParty>

    @Query("""
        SELECT distinct p
        FROM PersistentParty as p
        LEFT JOIN FETCH p.expenses
        WHERE p.id in (:ids)
    """)
    fun findPartiesWithExpenses(@Param("ids") ids: Set<Long>): List<PersistentParty>

    @Transactional
    @Modifying
    @Query("""
        INSERT INTO party_user (party_id, user_id)
        VALUES (:partyId, :userId)
    """, nativeQuery = true)
    fun addParticipant(@Param("partyId") partyId: Long, @Param("userId") userId: Long)

    @Transactional
    @Modifying
    @Query("""
        DELETE FROM party_user as pu
        WHERE pu.user_id = :userId AND pu.party_id = :partyId
    """, nativeQuery = true)
    fun removeParticipant(@Param("partyId") partyId: Long, @Param("userId") userId: Long)

}
