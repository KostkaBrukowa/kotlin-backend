package com.example.graphql.domain.party

import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.party.toResponse
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class PartyService(
        private val partyRepository: PartyRepository,
        private val partyRequestRepository: PartyRequestRepository
) {
    // GET
    fun getAllParties(userId: Long) = partyRepository.getAllByOwnerId(userId)

    fun getSingleParty(partyId: Long) = partyRepository.getTopById(partyId)

    fun findPartiesWithParticipants(partiesIds: Set<Long>) = partyRepository.findPartiesWithParticipants(partiesIds)


    // CREATE
    fun createParty(party: Party, userId: Long): Party {
        val currentUser = User(id = userId)
        val participants = (party.participants + currentUser).distinctBy { it.id }

        val newParty = partyRepository.saveNewParty(party.copy(owner = currentUser, participants = participants))

        partyRequestRepository.createPartyRequestsForParticipants(participants - currentUser, newParty)

        return newParty
    }

    // UPDATE
    fun updateParty(id: Long, party: Party) = partyRepository.updateParty(party.copy(id = id))

    fun removeParticipant(partyId: Long, participantId: Long, currentUserId: Long): Boolean {
        val party = partyRepository.getPartyWithOwnerAndParticipants(partyId)
                ?: throw EntityNotFoundException("Party")

        if (party.owner?.id == participantId) {
            return false
        }

        requirePartyOwnerOrParticipant(party, participantId, currentUserId)

        partyRepository.removeParticipant(partyId, participantId)

        return true
    }

    fun addParticipant(partyId: Long, participantId: Long, currentUserId: Long): Boolean {
        partyRepository.addParticipant(partyId, participantId)

        return true
    }

    // DELETE
    fun deleteParty(id: Long, currentUserId: Long): Boolean {
        if (partyRepository.getTopById(id)?.owner?.id != currentUserId) {
            throw UnauthorisedException()
        }

        partyRepository.removeParty(id)

        return true
    }

    private fun requirePartyOwnerOrParticipant(party: Party, participantId: Long, currentUserId: Long) {
        if (party.owner == null) throw InternalError("Party owner was not fetched from DB")
        if (party.owner.id != currentUserId && participantId != currentUserId) throw UnauthorisedException()
    }
}
