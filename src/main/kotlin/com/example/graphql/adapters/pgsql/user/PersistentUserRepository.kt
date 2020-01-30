package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.user.PersistentUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersistentUserRepository : JpaRepository<PersistentUser, Long> {
    fun findTopByEmail(email: String): PersistentUser?

    @Query("""
        SELECT u.id, u.name, u.email, u.bank_account, u.password, u.is_email_confirmed
        FROM party_user as pu
        INNER JOIN users as u
            ON pu.user_id = u.id
        WHERE pu.party_id = :partyId
    """, nativeQuery = true)
    fun findAllPartyParticipants(@Param("partyId") partyId: Long): List<PersistentUser>

    @Query("""
        SELECT u
        FROM PersistentUser as u
        JOIN FETCH u.partyRequests as pr
        WHERE pr.party.id = (:partyId)
    """)
    fun xxx(@Param("partyId") partyId: Long): PersistentUser
}
