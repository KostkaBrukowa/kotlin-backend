package com.example.graphql.domain.party

interface PartyRepository {
    fun getAllByOwnerId(id: Long): List<Party>

    fun getTopById(id: Long): Party?

    fun getPartyWithOwnerAndParticipants(id: Long): Party?

    fun saveNewParty(party: Party): Party

    fun updateParty(updatedParty: Party): Party

    fun removeParty(id: Long)

    fun findPartiesWithParticipants(partiesIds: Set<Long>): List<Party>

    fun findPartiesWithPartyRequests(partiesIds: Set<Long>): List<Party>

    fun findPartiesWithExpenses(partiesIds: Set<Long>): List<Party>

    fun removeParticipant(partyId: Long, participantId: Long)

    fun addParticipant(partyId: Long, participantId: Long)
}
