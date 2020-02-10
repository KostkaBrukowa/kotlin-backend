package com.example.graphql.adapters.pgsql.partyrequest

import com.example.graphql.domain.partyrequest.PartyRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import javax.transaction.Transactional

interface PersistentPartyRequestRepository : JpaRepository<PersistentPartyRequest, Long> {

    fun findAllByPartyId(partyId: Long): List<PersistentPartyRequest>

    fun findAllByUserId(userId: Long): List<PersistentPartyRequest>

    fun findByUserIdAndPartyId(userId: Long, partyId: Long): PersistentPartyRequest?

    @Transactional
    @Modifying
    @Query("""
        UPDATE PersistentPartyRequest
        SET status = :status
        WHERE id = :requestId
    """)
    fun updateStatus(@Param("requestId") requestId: Long, @Param("status") status: PartyRequestStatus)
}

