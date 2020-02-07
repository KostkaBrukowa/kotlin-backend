package com.example.graphql.domain.user

import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import org.springframework.stereotype.Component

@Component
class UserDataLoaderService(private val userRepository: UserRepository) {

    fun userToPartyRequestsDataLoaderMap(userIds: Set<Long>): Map<Long, List<PartyRequestType>> {
        val users = userRepository.findUsersWithPartyRequests(userIds)

        return users.associateBy({ it.id }, { it.partyRequests.map { participant -> participant.toResponse() } })
    }
}
