package com.example.graphql.adapters.pgsql.party

import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.message.Message
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.party.toPersistentEntity
import com.example.graphql.domain.user.toPersistentEntity
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class PgSqlPartyRepository(private val partyRepository: PersistentPartyRepository, private val userRepository: PersistentUserRepository) : PartyRepository {

    @Transactional
    override fun getAllByOwnerId(id: Long): List<Party> = partyRepository.getAllByOwnerId(id).map {
        it.toDomainWithRelations()
    }

    override fun getAllUsersPartiesWithParticipants(userId: Long): List<Party> {
        val userWithParties = userRepository.findUsersWithJoinedParties(setOf(userId)).firstOrNull() ?: throw EntityNotFoundException("user")

        if(userWithParties.joinedParties.isEmpty()) {
            return emptyList()
        }

        return partyRepository.findPartiesWithParticipants(userWithParties.joinedParties.map { it.id }).map {
            it.toDomainWithRelations().copy(
                    participants = it.participants.map { participant -> participant.toDomain() }
            )
        }
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
        val partyToUpdate = partyRepository.getTopById(updatedParty.id)?.copy(
                name = updatedParty.name,
                description = updatedParty.description ?: "",
                startDate = updatedParty.startDate,
                endDate = updatedParty.endDate,
                locationLatitude = updatedParty.locationLatitude,
                locationLongitude = updatedParty.locationLongitude,
                locationName = updatedParty.locationName

        ) ?: throw EntityNotFoundException("Party not found")

        return partyRepository.save(partyToUpdate).toDomainWithRelations()
    }

    override fun removeParty(id: Long) = partyRepository.deleteById(id)
}

private fun PersistentParty.toDomainWithRelations() = this.toDomain().copy(owner = this.owner?.toDomain())
