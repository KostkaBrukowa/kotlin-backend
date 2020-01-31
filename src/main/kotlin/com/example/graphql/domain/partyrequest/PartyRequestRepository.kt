package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User

interface PartyRequestRepository {

    fun createPartyRequestsForParticipants(participants: List<User>, party: Party): List<PartyRequest>
    fun findAllByParty(partyId: String): List<PartyRequest>
    fun findAllByUserId(userId: String): List<PartyRequest>
    fun findByUserIdAndPartyId(userId: String, partyId: String): PartyRequest?
}
