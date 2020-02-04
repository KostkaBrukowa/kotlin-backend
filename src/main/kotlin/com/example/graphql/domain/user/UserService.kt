package com.example.graphql.domain.user

import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class UserService(
        private val userRepository: UserRepository,
        private val partyRepository: PartyRepository
) {

    fun getUserById(id: Long): User? {
        return userRepository.getUserById(id)
    }

    fun getAllPartyParticipants(partyId: Long): List<User> {
        return userRepository.findAllPartyParticipants(partyId)
    }

    fun findAllParticipantsByPartiesIds(partiesIds: Set<Long>): Map<Long, List<UserType>> {
        val parties = partyRepository.findPartiesWithParticipants(partiesIds)

        return parties.associateBy({ it.id }, { it.participants.map { participant -> participant.toResponse() } })
    }
}

