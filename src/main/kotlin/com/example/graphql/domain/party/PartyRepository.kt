package com.example.graphql.domain.party

import com.example.graphql.domain.message.Message

interface PartyRepository {
    fun getAllByOwnerId(id: Long): List<Party>
    fun getAllUsersPartiesWithParticipants(userId: Long): List<Party>

    fun getTopById(id: Long): Party?

    fun getPartyWithOwnerAndParticipants(id: Long): Party?

    fun saveNewParty(party: Party): Party

    fun updateParty(updatedParty: Party): Party

    fun removeParty(id: Long)

    fun findPartiesWithParticipants(partiesIds: Set<Long>): List<Party>
    fun findPartiesWithPartyRequests(partiesIds: Set<Long>): List<Party>
    fun findPartiesWithExpenses(partiesIds: Set<Long>): List<Party>
    fun findPartiesWithMessages(ids: Set<Long>): Map<Party, List<Message>>

    fun removeParticipant(partyId: Long, participantId: Long)

    fun addParticipant(partyId: Long, participantId: Long)
}
