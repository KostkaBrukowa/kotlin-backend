package com.example.graphql.domain.party

interface PartyRepository {
    fun getAllByOwnerId(id: Long): List<Party>

    fun getTopById(id: Long): Party?

    fun saveNewParty(party: Party): Party

    fun updateParty(updatedParty: Party): Party

    fun removeParty(id: String): Unit
}
