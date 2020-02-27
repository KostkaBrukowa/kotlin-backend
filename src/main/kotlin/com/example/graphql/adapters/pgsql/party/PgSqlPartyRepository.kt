package com.example.graphql.adapters.pgsql.party

import com.example.graphql.domain.message.Message
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.party.toPersistentEntity
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class PgSqlPartyRepository(private val partyRepository: PersistentPartyRepository) : PartyRepository {

    @Transactional
    override fun getAllByOwnerId(id: Long): List<Party> = partyRepository.getAllByOwnerId(id).map {
        it.toDomainWithRelations()
    }

    @Transactional
    override fun getTopById(id: Long): Party? = partyRepository.getTopById(id)?.toDomainWithRelations()

    override fun getPartyWithOwnerAndParticipants(id: Long): Party? {
        val party = partyRepository.findPartiesWithParticipants(setOf(id)).firstOrNull()

        return party?.toDomainWithRelations()?.copy(
                participants = party.participants.map { it.toDomain() }
        )
    }

    override fun findPartiesWithParticipants(partiesIds: Set<Long>): List<Party> {
        return partyRepository.findPartiesWithParticipants(partiesIds).map {
            it.toDomain().copy(participants = it.participants.map { participant -> participant.toDomain() })
        }
    }

    override fun findPartiesWithPartyRequests(partiesIds: Set<Long>): List<Party> {
        return partyRepository.findPartiesWithPartyRequests(partiesIds).map {
            it.toDomain().copy(partyRequests = it.partyRequests.map { request -> request.toDomain() })
        }
    }

    override fun findPartiesWithExpenses(partiesIds: Set<Long>): List<Party> {
        return partyRepository.findPartiesWithExpenses(partiesIds).map {
            it.toDomain().copy(expenses = it.expenses.map { expense -> expense.toDomain() })
        }
    }

    override fun findPartiesWithMessages(ids: Set<Long>): Map<Party, List<Message>> {
        return partyRepository
                .findPartiesWithMessages(ids)
                .associateBy({ it.toDomain() }, { it.messages.map { message -> message.toDomain() } })
    }

    override fun removeParticipant(partyId: Long, participantId: Long) {
        partyRepository.removeParticipant(partyId, participantId)
    }

    override fun addParticipant(partyId: Long, participantId: Long) {
        partyRepository.addParticipant(partyId, participantId)
    }

    @Transactional
    override fun saveNewParty(party: Party): Party {
        val newParty = party.toPersistentEntity().copy(
                owner = party.owner?.toPersistentEntity(),
                participants = party.participants.map {
                    it.toPersistentEntity()
                }.toSet()
        )

        val savedParty = partyRepository.save(newParty)

        return savedParty.toDomainWithRelations()
    }

    override fun updateParty(updatedParty: Party): Party {
        val partyToUpdate = partyRepository.getTopById(updatedParty.id)?.apply {
            name = updatedParty.name
            description = updatedParty.description ?: ""
            startDate = updatedParty.startDate
            endDate = updatedParty.endDate
        } ?: throw Exception("Party not found")

        return partyRepository.save(partyToUpdate).toDomainWithRelations()
    }

    override fun removeParty(id: Long) = partyRepository.deleteById(id)
}

private fun PersistentParty.toDomainWithRelations() = this.toDomain().copy(owner = this.owner?.toDomain())
