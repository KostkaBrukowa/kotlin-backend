package com.example.graphql.adapters.pgsql.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.toPersistentEntity
import org.springframework.stereotype.Component

@Component
class PgSqlPartyRepository(private val persistentPartyRepository: PersistentPartyRepository) : PartyRepository {

    override fun getAllByOwnerId(id: Long): List<Party> = persistentPartyRepository.getAllByOwnerId(id).map { it.toDomain() }

    override fun getTopById(id: Long): Party? = persistentPartyRepository.getTopById(id)?.toDomain()

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

    override fun removeParty(id: String): Unit = persistentPartyRepository.deleteById(id.toLong())
}
