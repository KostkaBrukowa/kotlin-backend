package com.example.graphql.adapters.pgsql.partyrequest

import com.example.graphql.adapters.pgsql.utils.toNullable
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
        private val persistentPartyRequestRepository: PersistentPartyRequestRepository
) : PartyRequestRepository {
    override fun createPartyRequestsForParticipants(participants: List<User>, party: Party): List<PartyRequest> {
        val persistentParty = party.toPersistentEntity()
        return persistentPartyRequestRepository.saveAll(participants.map {
            PersistentPartyRequest(
                    user = it.toPersistentEntity(),
                    party = persistentParty,
                    status = PartyRequestStatus.IN_PROGRESS
            )
        }).map { it.toDomainWithRelations() }
    }

    override fun findAllByParty(partyId: Long): List<PartyRequest> =
            persistentPartyRequestRepository.findAllByPartyId(partyId = partyId).map { it.toDomainWithRelations() }

    override fun findAllByUserId(userId: Long): List<PartyRequest> =
            persistentPartyRequestRepository.findAllByUserId(userId).map { it.toDomainWithRelations() }

    override fun findByUserIdAndPartyId(userId: Long, partyId: Long): PartyRequest? {
        return persistentPartyRequestRepository.findByUserIdAndPartyId(userId, partyId)?.toDomainWithRelations()
    }

    override fun findByIdWithUser(partyRequestId: Long): PartyRequest? =
            persistentPartyRequestRepository.findById(partyRequestId).toNullable()?.toDomainWithRelations()

    override fun updateStatus(partyRequest: PartyRequest): Boolean {
        persistentPartyRequestRepository.updateStatus(partyRequest.id, partyRequest.status)

        return true
    }

    override fun findByIdWithUserAndPartyOwner(partyRequestId: Long): PartyRequest? {
        val request = persistentPartyRequestRepository.findById(partyRequestId).toNullable()

        return request?.toDomainWithRelations()?.copy(
                party = request.party?.toDomain()?.copy(owner = request.party.owner?.toDomain())
        )
    }

    override fun remove(request: PartyRequest): Boolean {
        persistentPartyRequestRepository.deleteById(request.id)

        return true
    }

    override fun findPartyRequestsWithParties(ids: Set<Long>): List<PartyRequest> {
        return persistentPartyRequestRepository.findAllById(ids).map { it.toDomainWithRelations() }
    }

    override fun findPartyRequestsWithUsers(ids: Set<Long>): List<PartyRequest> {
        return persistentPartyRequestRepository.findAllById(ids).map { it.toDomainWithRelations() }
    }
}

private fun PersistentPartyRequest.toDomainWithRelations() = this.toDomain().copy(
        user = this.user?.toDomain(),
        party = this.party?.toDomain()
)
