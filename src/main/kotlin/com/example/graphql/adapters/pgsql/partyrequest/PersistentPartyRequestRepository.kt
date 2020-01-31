package com.example.graphql.adapters.pgsql.partyrequest

import org.springframework.data.jpa.repository.JpaRepository

interface PersistentPartyRequestRepository : JpaRepository<PersistentPartyRequest, Long> {

    fun findAllByPartyId(partyId: Long): List<PersistentPartyRequest>

    fun findAllByUserId(userId: Long): List<PersistentPartyRequest>

    fun findByUserIdAndPartyId(userId: Long, partyId: Long): PersistentPartyRequest?
}

