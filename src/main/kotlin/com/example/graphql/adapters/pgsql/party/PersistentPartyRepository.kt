package com.example.graphql.adapters.pgsql.party

import com.example.graphql.domain.party.PersistentParty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersistentPartyRepository : JpaRepository<PersistentParty, Long> {
    fun getAllByOwnerId(id: Long): List<PersistentParty>
    fun getTopById(id: Long): PersistentParty?

    @Query("""
        SELECT p
        FROM PersistentParty as p
        LEFT JOIN FETCH p.participants as ps
        WHERE p.id = :partyId
    """)
    fun getPartyWithOwnerAndParticipants(partyId: Long): PersistentParty?

    @Query("""
        SELECT distinct p
        FROM PersistentParty as p
        LEFT JOIN FETCH p.partyRequests as pr
        WHERE p.id IN (:partiesIds)
    """)
    fun findPartiesWithPartyRequests(@Param("partiesIds") partiesIds: List<Long>): List<PersistentParty>

    @Query("""
        SELECT distinct p
        FROM PersistentParty as p
        LEFT JOIN FETCH p.participants
        WHERE p.id in (:partiesIds)
    """)
    fun findPartiesWithParticipants(@Param("partiesIds") partiesIds: List<Long>): List<PersistentParty>
}
