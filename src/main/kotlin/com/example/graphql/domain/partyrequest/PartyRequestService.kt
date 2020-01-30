package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User
import org.springframework.stereotype.Component

@Component
class PartyRequestService(private val partyRequestRepository: PartyRequestRepository) {

    fun sendRequestsForPartyParticipants(participants: List<User>, party: Party) {
        partyRequestRepository.createPartyRequestsParticipants(participants, party)
    }

    fun getAllPartyRequestsByPartyId(partyId: String) = partyRequestRepository.findAllByParty(partyId)

    fun getAllPartyRequestsByUserId(userId: String) = partyRequestRepository.findAllByUserId(userId)
}
