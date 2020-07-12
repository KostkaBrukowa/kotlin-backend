package com.example.graphql.domain.party

import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.domain.user.User
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component
import java.lang.Exception

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

        val partyToBePersisted = party.copy(owner = currentUser, participants = participants)
        validatePartyType(partyToBePersisted)
        val newParty = partyRepository.saveNewParty(partyToBePersisted)

        val partyRequests = partyRequestRepository.createPartyRequestsForParticipants(participants - currentUser, newParty)

        if(newParty.owner == null) throw InternalError("Party was not entirely fetched")
        notificationService.newPartyRequestsNotifications(partyRequests, newParty.owner.id, party.name) // TODO

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

    private fun validatePartyType(partyToBePersisted: Party) {
        when(partyToBePersisted.type) {
            PartyKind.EVENT -> {
                checkNotNull(partyToBePersisted.startDate)
                checkNotNull(partyToBePersisted.endDate)
                checkNotNull(partyToBePersisted.name)
                checkNotNull(partyToBePersisted.locationName)
                checkNotNull(partyToBePersisted.locationLatitude)
                checkNotNull(partyToBePersisted.locationLongitude)
            }
            PartyKind.GROUP -> {
                checkNotNull(partyToBePersisted.name)
                if(partyToBePersisted.endDate != null)
                    throw Exception("End date cannot be defined in group party")
            }
            PartyKind.FRIENDS -> {
                if(partyToBePersisted.name != null)
                    throw Exception("Name cannot be defined in friends party")
                if(partyToBePersisted.endDate != null)
                    throw Exception("End date cannot be defined in friends party")
            }
        }
    }

}
