package com.example.graphql.domain.party

import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.User
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class PartyService(
        private val partyRepository: PartyRepository,
        private val partyRequestService: PartyRequestService
) {
    fun getTestParty(): Party {
        return Party(
                name = "test name",
                startDate = ZonedDateTime.now()
        )
    }

    fun getAllParties(userId: String): List<Party> {
        return partyRepository.getAllByOwnerId(userId.toLong())
    }

    fun getSingleParty(partyId: String): Party? {
        return partyRepository.getTopById(partyId.toLong())
    }

    fun createParty(party: Party, userId: String): Party {
        val currentUser = User(id = userId)
        val participants = (party.participants + currentUser).distinctBy { it.id }

        val newParty = partyRepository.saveNewParty(party.copy(owner = currentUser, participants = participants))

        partyRequestService.sendRequestsForPartyParticipants(participants - currentUser, newParty)

        return newParty
    }

    fun updateParty(id: String, party: Party): Party {
        return partyRepository.updateParty(party.copy(id = id))
    }

    fun deleteParty(id: String, currentUserId: String): Boolean {
        if (partyRepository.getTopById(id.toLong())?.owner?.id != currentUserId) {
            throw UnauthorisedException()
        }

        partyRepository.removeParty(id)

        return true
    }
}
