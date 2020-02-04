package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User

interface PartyRequestRepository {

    fun createPartyRequestsForParticipants(participants: List<User>, party: Party): List<PartyRequest>
    fun findAllByParty(partyId: Long): List<PartyRequest>
    fun findAllByUserId(userId: Long): List<PartyRequest>
    fun findByUserIdAndPartyId(userId: Long, partyId: Long): PartyRequest?
}
