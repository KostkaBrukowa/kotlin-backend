package com.example.graphql.adapters.pgsql.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.toPersistentEntity
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class PgSqlPartyRepository(private val persistentPartyRepository: PersistentPartyRepository) : PartyRepository {

    @Transactional
    override fun getAllByOwnerId(id: String): List<Party> = persistentPartyRepository.getAllByOwnerId(id.toLong()).map { it.toDomain() }

    @Transactional
    override fun getTopById(id: String): Party? = persistentPartyRepository.getTopById(id.toLong())?.toDomain()

    override fun getPartyWithOwnerAndParticipants(partyId: String): Party? {
        return persistentPartyRepository.getPartyWithOwnerAndParticipants(partyId.toLong())?.toDomain()
    }

    override fun findPartiesWithParticipants(partiesIds: Set<String>): List<Party> {
        return persistentPartyRepository.findPartiesWithParticipants(partiesIds.map { it.toLong() }).map { it.toDomain() }
    }

    override fun findPartiesWithPartyRequests(partiesIds: Set<String>): List<Party> {
        return persistentPartyRepository.findPartiesWithPartyRequests(partiesIds.map { it.toLong() }).map { it.toDomain() }
    }

    override fun saveNewParty(party: Party): Party = persistentPartyRepository.save(party.toPersistentEntity()).toDomain()

    override fun updateParty(updatedParty: Party): Party {
        val partyToUpdate = persistentPartyRepository.getTopById(updatedParty.id.toLong())?.apply {
            name = updatedParty.name
            description = updatedParty.description ?: ""
            startDate = updatedParty.startDate
            endDate = updatedParty.endDate
        } ?: throw Exception("Party not found")

        return persistentPartyRepository.save(partyToUpdate).toDomain()
    }

    override fun removeParty(id: String) = persistentPartyRepository.deleteById(id.toLong())
}
