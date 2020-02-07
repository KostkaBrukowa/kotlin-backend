package com.example.graphql.domain.user

import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class UserService(
        private val userRepository: UserRepository
) {

    fun getUserById(id: Long): User? {
        return userRepository.getUserById(id)
    }

    fun getAllPartyParticipants(partyId: Long): List<User> {
        return userRepository.findAllPartyParticipants(partyId)
    }

}

