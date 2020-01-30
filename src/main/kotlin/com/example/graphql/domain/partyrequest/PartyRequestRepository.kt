package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User

interface PartyRequestRepository {

    fun createPartyRequestsParticipants(participants: List<User>, party: Party)
    fun findAllByParty(partyId: String): List<PartyRequest>
    fun findAllByUserId(userId: String): List<PartyRequest>
}
