package com.example.graphql.domain.party

interface PartyRepository {
    fun getAllByOwnerId(id: String): List<Party>

    fun getTopById(id: String): Party?

    fun getPartyWithOwnerAndParticipants(id: String): Party?

    fun saveNewParty(party: Party): Party

    fun updateParty(updatedParty: Party): Party

    fun removeParty(id: String)

    fun findPartiesWithParticipants(partiesIds: Set<String>): List<Party>

    fun findPartiesWithPartyRequests(partiesIds: Set<String>): List<Party>
}
