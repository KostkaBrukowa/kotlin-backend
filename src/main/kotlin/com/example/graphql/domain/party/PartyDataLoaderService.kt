package com.example.graphql.domain.party

import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class PartyDataLoaderService(private val partyRepository: PartyRepository) {

    fun partyToPartyRequestsDataLoaderMap(partiesIds: Set<Long>): Map<Long, List<PartyRequestType>> {
        val parties = partyRepository.findPartiesWithParticipants(partiesIds)

        return parties.associateBy({ it.id }, { it.partyRequests.map { participant -> participant.toResponse() } })
    }


    fun partyToParticipantsDataLoaderMap(partiesIds: Set<Long>): Map<Long, List<UserType>> {
        val parties = partyRepository.findPartiesWithParticipants(partiesIds)

        return parties.associateBy({ it.id }, { it.participants.map { participant -> participant.toResponse() } })
    }
}