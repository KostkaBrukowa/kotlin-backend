package com.example.graphql.adapters.pgsql.partyrequest

import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.toPersistentEntity
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.stereotype.Component

@Component
class PgSqlPartyRequestRepository(
        private val persistentPartyRequestRepository: PersistentPartyRequestRepository,
        private val persistentPartyRepository: PersistentPartyRepository
) : PartyRequestRepository {
    override fun createPartyRequestsForParticipants(participants: List<User>, party: Party): List<PartyRequest> {
        val persistentParty = party.toPersistentEntity()
        return persistentPartyRequestRepository.saveAll(participants.map {
            PersistentPartyRequest(
                    user = it.toPersistentEntity(),
                    party = persistentParty,
                    status = PartyRequestStatus.IN_PROGRESS
            )
        }).map { it.toDomain() }
    }

    override fun findAllByParty(partyId: Long): List<PartyRequest> =
            persistentPartyRequestRepository.findAllByPartyId(partyId = partyId).map { it.toDomain() }

    override fun findAllByUserId(userId: Long): List<PartyRequest> =
            persistentPartyRequestRepository.findAllByUserId(userId).map { it.toDomain() }

    override fun findByUserIdAndPartyId(userId: Long, partyId: Long): PartyRequest? {
        return persistentPartyRequestRepository.findByUserIdAndPartyId(userId, partyId)?.toDomain()
    }

    override fun findByIdWithUser(partyRequestId: Long): PartyRequest {
        val request = persistentPartyRequestRepository.getOne(partyRequestId)

        return request.toDomain().copy(user = request.user?.toDomain(), party = request.party?.toDomain())
    }

    override fun updateStatus(partyRequest: PartyRequest): Boolean {
        persistentPartyRequestRepository.updateStatus(partyRequest.id, partyRequest.status)

        return true
    }

    override fun findByIdWithUserAndPartyOwner(partyRequestId: Long): PartyRequest? {
        val request = persistentPartyRequestRepository.findByIdWithUserAndPartyOwner(partyRequestId)

        return request?.toDomain()?.copy(
                user = request.user?.toDomain(),
                party = request.party?.toDomain()?.copy(owner = request.party.owner?.toDomain())
        )
    }

    override fun remove(request: PartyRequest): Boolean {
        persistentPartyRequestRepository.delete(request.toPersistentEntity())

        return true
    }

    override fun findPartyRequestsWithParties(ids: Set<Long>): List<PartyRequest> {
        return persistentPartyRequestRepository.findAllById(ids).map { it.toDomain().copy(party = it.party?.toDomain()) }
    }

    override fun findPartyRequestsWithUsers(ids: Set<Long>): List<PartyRequest> {
        return persistentPartyRequestRepository.findAllById(ids).map { it.toDomain().copy(user = it.user?.toDomain()) }
    }
}
