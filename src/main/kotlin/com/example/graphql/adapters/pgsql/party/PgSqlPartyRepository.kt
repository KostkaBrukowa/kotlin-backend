package com.example.graphql.adapters.pgsql.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.toPersistentEntity
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class PgSqlPartyRepository(private val persistentPartyRepository: PersistentPartyRepository) : PartyRepository {

    @Transactional
    override fun getAllByOwnerId(id: Long): List<Party> = persistentPartyRepository.getAllByOwnerId(id).map {
        it.toDomain().copy(owner = it.owner?.toDomain())
    }

    @Transactional
    override fun getTopById(id: Long): Party? {
        val party = persistentPartyRepository.getTopById(id)

        return party?.toDomain()?.copy(owner = party.owner?.toDomain())
    }

    override fun getPartyWithOwnerAndParticipants(id: Long): Party? {
        val party = persistentPartyRepository.getPartyWithOwnerAndParticipants(id)

        return party?.toDomain()?.copy(
                owner = party.owner?.toDomain(),
                participants = party.participants.map { it.toDomain() }
        )
    }

    override fun findPartiesWithParticipants(partiesIds: Set<Long>): List<Party> {
        return persistentPartyRepository.findPartiesWithParticipants(partiesIds.toList()).map {
            it.toDomain().copy(participants = it.participants.map { participant -> participant.toDomain() })
        }
    }

    override fun findPartiesWithPartyRequests(partiesIds: Set<Long>): List<Party> {
        return persistentPartyRepository.findPartiesWithPartyRequests(partiesIds.toList()).map {
            it.toDomain().copy(partyRequests = it.partyRequests.map { request -> request.toDomain() })
        }
    }

    override fun saveNewParty(party: Party): Party {
        val newParty = party.toPersistentEntity().copy(
                owner = party.owner?.toPersistentEntity(),
                participants = party.participants.map {
                    it.toPersistentEntity()
                }
        )

        return persistentPartyRepository.save(newParty).toDomain()
    }

    override fun updateParty(updatedParty: Party): Party {
        val partyToUpdate = persistentPartyRepository.getTopById(updatedParty.id)?.apply {
            name = updatedParty.name
            description = updatedParty.description ?: ""
            startDate = updatedParty.startDate
            endDate = updatedParty.endDate
        } ?: throw Exception("Party not found")

        return persistentPartyRepository.save(partyToUpdate).toDomain()
    }

    override fun removeParty(id: Long) = persistentPartyRepository.deleteById(id)
}
