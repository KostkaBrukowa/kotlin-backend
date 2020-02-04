package com.example.graphql.adapters.pgsql.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.toPersistentEntity
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.stereotype.Component

@Component
class PgSqlPartyRequestRepository(private val persistentPartyRequestRepository: PersistentPartyRequestRepository) : PartyRequestRepository {
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
}
