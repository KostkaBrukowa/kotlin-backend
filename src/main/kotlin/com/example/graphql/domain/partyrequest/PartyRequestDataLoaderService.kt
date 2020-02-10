package com.example.graphql.domain.partyrequest

import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.party.toResponse
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class PartyRequestDataLoaderService(private val partyRequestRepository: PartyRequestRepository) {

    fun partyRequestToPartyDataLoaderMap(ids: Set<Long>): Map<Long, PartyType> {
        val partyRequests = partyRequestRepository.findPartyRequestsWithParties(ids)

        return partyRequests.associateBy({ it.id }, { it.party!!.toResponse() })
    }


    fun partyRequestToUserDataLoaderMap(ids: Set<Long>): Map<Long, UserType> {
        val partyRequests = partyRequestRepository.findPartyRequestsWithUsers(ids)

        return partyRequests.associateBy({ it.id }, { it.user!!.toResponse() })
    }
}
