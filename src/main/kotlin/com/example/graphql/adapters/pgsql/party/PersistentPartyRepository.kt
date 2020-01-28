package com.example.graphql.adapters.pgsql.party

import com.example.graphql.domain.party.PersistentParty
import org.springframework.data.jpa.repository.JpaRepository

interface PersistentPartyRepository : JpaRepository<PersistentParty, Long> {
    fun getAllByOwnerId(id: Long): List<PersistentParty>

    fun getTopById(id: Long): PersistentParty?
}
