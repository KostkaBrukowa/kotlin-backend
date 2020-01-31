package com.example.graphql.domain.user

import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.PartyService
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class UserService(
        private val userRepository: UserRepository,
        private val partyRepository: PartyRepository
) {

    fun getUserById(id: String): User? {
        return userRepository.getUserById(id)
    }

    fun getAllPartyParticipants(partyId: String): List<User> {
        return userRepository.findAllPartyParticipants(partyId)
    }

    fun findUsersByIds(usersIds: List<String>): List<User> {
        return userRepository.findUsersById(usersIds)
    }

    fun findAllParticipantsByPartiesIds(partiesIds: Set<String>): Map<String, List<UserType>> {
        val parties = partyRepository.findPartiesWithParticipants(partiesIds)

        return parties.associateBy({ it.id }, { it.participants.map { participant -> participant.toResponse() } })
    }
}

