package com.example.graphql.domain.party

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.User
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class PartyService(
        private val partyRepository: PartyRepository,
        private val partyRequestService: PartyRequestService
) {
    // GET
    fun getAllParties(userId: Long) = partyRepository.getAllByOwnerId(userId)

    fun getSingleParty(partyId: Long) = partyRepository.getTopById(partyId)

    // CREATE
    fun createParty(party: Party, userId: Long): Party {
        val currentUser = User(id = userId)
        val participants = (party.participants + currentUser).distinctBy { it.id }

        val newParty = partyRepository.saveNewParty(party.copy(owner = currentUser, participants = participants))

        partyRequestService.sendRequestsForPartyParticipants(participants - currentUser, newParty)

        return newParty
    }

    // UPDATE
    fun updateParty(id: Long, party: Party) = partyRepository.updateParty(party.copy(id = id))

    // DELETE
    fun deleteParty(id: Long, currentUserId: Long): Boolean {
        if (partyRepository.getTopById(id)?.owner?.id != currentUserId) {
            throw UnauthorisedException()
        }

        partyRepository.removeParty(id)

        return true
    }

    fun removeParticipant(partyId: Long, participantId: Long, context: AppGraphQLContext): Boolean {
        TODO("")
    }

}
