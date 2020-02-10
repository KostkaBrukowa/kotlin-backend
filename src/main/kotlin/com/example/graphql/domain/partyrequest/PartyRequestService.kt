package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.PartyService
import com.example.graphql.domain.party.requirePartyOwner
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class PartyRequestService(
        private val partyRequestRepository: PartyRequestRepository,
        private val partyRepository: PartyRepository,
        private val partyService: PartyService
) {
    // GET
    fun getAllPartyRequestsByPartyId(partyId: Long) = partyRequestRepository.findAllByParty(partyId)

    fun getAllPartyRequestsByUserId(userId: Long, currentUserId: Long): List<PartyRequest> {
        if(userId != currentUserId) throw UnauthorisedException()

        return partyRequestRepository.findAllByUserId(userId)
    }

    // UPDATE
    fun sendRequestsForPartyParticipants(participants: List<User>, party: Party): List<PartyRequest> {
        return partyRequestRepository.createPartyRequestsForParticipants(participants, party)
    }

    fun sendPartyRequest(requestReceiverId: Long, partyId: Long, currentUserId: Long): PartyRequest? {
        val party = partyRepository.getPartyWithOwnerAndParticipants(partyId)
                ?: throw EntityNotFoundException("Party")

        requirePartyOwner(party, currentUserId)

        if (requestReceiverId == currentUserId || participantAlreadyExists(party, requestReceiverId)) {
            return null
        }

        return sendRequestsForPartyParticipants(listOf(User(requestReceiverId)), Party(id = partyId))
                .first()
    }

    fun acceptRequest(partyRequestId: Long, currentUserId: Long): Boolean {
        val request = changeRequestStatus(partyRequestId, currentUserId, PartyRequestStatus.ACCEPTED)

        if (request.party != null) {
            partyService.addParticipant(request.party.id, currentUserId, currentUserId)
        }

        return true
    }


    fun declineRequest(partyRequestId: Long, currentUserId: Long): Boolean {
        changeRequestStatus(partyRequestId, currentUserId, PartyRequestStatus.DECLINED)

        return true
    }

    //REMOVE
    fun removePartyRequest(partyRequestId: Long, currentUserId: Long): Boolean {
        val request = partyRequestRepository.findByIdWithUserAndPartyOwner(partyRequestId)
                ?: throw EntityNotFoundException("PartyRequest")

        requirePartyOwnerOrRequestOwner(request, currentUserId)

        return partyRequestRepository.remove(request)
    }

    private fun changeRequestStatus(partyRequestId: Long, currentUserId: Long, status: PartyRequestStatus): PartyRequest {
        val request = partyRequestRepository.findByIdWithUser(partyRequestId)
                ?: throw EntityNotFoundException("PartyRequest")

        requirePartyRequestChangeable(status, request, currentUserId)

        partyRequestRepository.updateStatus(request.copy(status = status))

        return request
    }

    private fun requirePartyOwnerOrRequestOwner(request: PartyRequest, currentUserId: Long) {
        if (request.party == null || request.user == null || request.party.owner == null)
            throw InternalError("Party request was not entirely fetched")

        if (request.user.id != currentUserId && request.party.owner.id != currentUserId) throw UnauthorisedException()
    }

    private fun participantAlreadyExists(party: Party, receiverId: Long): Boolean {
        return party.participants.any { it.id == receiverId }
    }

    private fun requirePartyRequestChangeable(status: PartyRequestStatus, request: PartyRequest, currentUserId: Long) {
        if (request.user?.id != currentUserId) throw UnauthorisedException()

        if (request.status == status) throw RequestStatusException(status)
    }

}

class RequestStatusException(status: PartyRequestStatus) : Exception("Request was already in status $status")
