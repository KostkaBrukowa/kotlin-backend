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
    fun getAllUserParties(userId: Long): List<Party> {
        return partyRepository.getAllUsersPartiesWithParticipants(userId)
    }

    fun getSingleParty(partyId: Long) = partyRepository.getTopById(partyId)


    // CREATE
    fun createParty(party: Party, userId: Long): Party {
        val currentUser = User(id = userId)
        val participants = (party.participants + currentUser).distinctBy { it.id }

        val partyToBePersisted = party.copy(owner = currentUser, participants = listOf(currentUser))
        validatePartyType(partyToBePersisted)
        val newParty = partyRepository.saveNewParty(partyToBePersisted)

        val partyRequests = partyRequestRepository.createPartyRequestsForParticipants(participants - currentUser, newParty)

        if(newParty.owner == null) throw InternalError("Party was not entirely fetched")
        notificationService.newPartyRequestsNotifications(partyRequests, newParty.owner.id, newParty.id, party.name) // TODO

        return newParty
    }

    // UPDATE
    fun updateParty(partyRequest: Party, currentUserId: Long): Party {
        val party = partyRepository.getTopById(partyRequest.id) ?: throw EntityNotFoundException("Party")

        requirePartyOwner(party, currentUserId)

        return partyRepository.updateParty(partyRequest.copy(id = party.id))
    }

    fun removeParticipant(partyId: Long, participantId: Long, currentUserId: Long): Party? {
        val party = partyRepository.getPartyWithOwnerAndParticipants(partyId)
                ?: throw EntityNotFoundException("Party")

        if (party.owner?.id == participantId) {
            return null
        }

        requirePartyOwnerOrParticipant(party, participantId, currentUserId)

        partyRepository.removeParticipant(partyId, participantId)

        return party
    }

    fun addParticipant(partyId: Long, participantId: Long, currentUserId: Long): Boolean {
        partyRepository.addParticipant(partyId, participantId)

        return true
    }

    // DELETE
    fun deleteParty(id: Long, currentUserId: Long): Party? {
        val party = partyRepository.getTopById(id)

        if (party?.owner?.id != currentUserId) {
            throw UnauthorisedException()
        }

        partyRepository.removeParty(id)

        return party
    }

    private fun validatePartyType(partyToBePersisted: Party) {
        when(partyToBePersisted.type) {
            PartyKind.EVENT -> {
                checkNotNull(partyToBePersisted.startDate) { "Start date was null for event party" }
                checkNotNull(partyToBePersisted.name){ "Name was null for event party" }
                checkNotNull(partyToBePersisted.locationName){ "Location name was null for event party" }
                checkNotNull(partyToBePersisted.locationLatitude){ "Location latitude was null for event party" }
                checkNotNull(partyToBePersisted.locationLongitude){ "Location longitude was null for event party" }
            }
            PartyKind.GROUP -> {
                checkNotNull(partyToBePersisted.name){ "Name was null for group party" }
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
