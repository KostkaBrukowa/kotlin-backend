package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import com.example.graphql.schema.exceptions.handlers.InvalidActionException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class PartyRequestService(
        private val partyRequestRepository: PartyRequestRepository,
        private val partyRepository: PartyRepository,
        private val userRepository: UserRepository
) {

    fun getAllPartyRequestsByPartyId(partyId: Long) = partyRequestRepository.findAllByParty(partyId)

    fun getAllPartyRequestsByUserId(userId: Long) = partyRequestRepository.findAllByUserId(userId)

    fun sendRequestsForPartyParticipants(participants: List<User>, party: Party) {
        partyRequestRepository.createPartyRequestsForParticipants(participants, party)
    }

    fun sendPartyRequest(requestReceiverId: Long, partyId: Long, currentUserId: Long): PartyRequest? {
        if (requestReceiverId == currentUserId) return null

        val party = partyRepository.getPartyWithOwnerAndParticipants(partyId)
                ?: throw InvalidActionException("Party with such id doesn't exist")

        if (party.owner?.id != currentUserId) throw UnauthorisedException()

        if (party.participants.any { it.id == requestReceiverId }) {
            return partyRequestRepository.findByUserIdAndPartyId(requestReceiverId, partyId)
        }

        return partyRequestRepository
                .createPartyRequestsForParticipants(listOf(User(requestReceiverId)), Party(id = partyId))
                .first()
    }

    fun acceptRequest(partyRequestId: Long, currentUserId: Long): PartyRequest {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun declineRequest(partyRequestId: Long, currentUserId: Long): PartyRequest {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun removePartyRequest(partyRequestId: Long, currentUserId: Long): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun findAllByPartiesIds(partiesIds: Set<Long>): Map<Long, List<PartyRequestType>> {
        val parties = partyRepository.findPartiesWithParticipants(partiesIds)

        return parties.associateBy({ it.id }, { it.partyRequests.map { participant -> participant.toResponse() } })
    }

    fun findAllByUsersIds(userIds: Set<Long>): Map<Long, List<PartyRequestType>> {
        val users = userRepository.findUsersWithPartyRequests(userIds)

        return users.associateBy({ it.id }, { it.partyRequests.map { participant -> participant.toResponse() } })
    }
}
