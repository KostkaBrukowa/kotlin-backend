package com.example.graphql.domain.party

import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.domain.user.User
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class PartyService(
        private val partyRepository: PartyRepository,
        private val partyRequestRepository: PartyRequestRepository,
        private val notificationService: NotificationService
) {
    // GET
    fun getAllParties(userId: Long) = partyRepository.getAllByOwnerId(userId)

    fun getSingleParty(partyId: Long) = partyRepository.getTopById(partyId)


    // CREATE
    fun createParty(party: Party, userId: Long): Party {
        val currentUser = User(id = userId)
        val participants = (party.participants + currentUser).distinctBy { it.id }

        val newParty = partyRepository.saveNewParty(party.copy(owner = currentUser, participants = participants))

        val partyRequests = partyRequestRepository.createPartyRequestsForParticipants(participants - currentUser, newParty)

        if(newParty.owner == null) throw InternalError("Party was not entirely fetched")
        notificationService.newPartyRequestsNotifications(partyRequests, newParty.owner!!.id, party.name) // TODO

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

}
