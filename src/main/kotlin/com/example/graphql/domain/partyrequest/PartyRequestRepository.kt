package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User

interface PartyRequestRepository {

    fun findAllByParty(partyId: Long): List<PartyRequest>
    fun findAllByUserId(userId: Long): List<PartyRequest>
    fun findByUserIdAndPartyId(userId: Long, partyId: Long): PartyRequest?
    fun findByIdWithUser(partyRequestId: Long): PartyRequest?
    fun findByIdWithUserAndPartyOwner(partyRequestId: Long): PartyRequest?
    fun findPartyRequestsWithParties(ids: Set<Long>): List<PartyRequest>
    fun findPartyRequestsWithUsers(ids: Set<Long>): List<PartyRequest>

    fun createPartyRequestsForParticipants(participants: List<User>, party: Party): List<PartyRequest>

    fun updateStatus(partyRequest: PartyRequest): Boolean

    fun remove(request: PartyRequest): Boolean
}
