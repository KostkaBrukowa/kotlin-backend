package com.example.graphql.domain.party

import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class PartyService(private val partyRepository: PartyRepository, private val userRepository: UserRepository) {
    fun getTestParty(): Party {
        return Party(
                name = "test name",
                startDate = ZonedDateTime.now()
        )
    }

    fun getAllParties(userId: String): List<Party> {
        return partyRepository.getAllByOwnerId(userId.toLong())
    }

    fun getSingleParty(partyId: String): Party? {
        return partyRepository.getTopById(partyId.toLong())
    }

    fun createParty(party: Party, userId: String): Party {
        val currentUser = User(id = userId)
        val participants = (party.participants + currentUser).distinctBy { it.id }

//        requestService.sendRequestsForParty(participants - currentUser) TODO when requests are done

        return partyRepository.saveNewParty(party.copy(owner = currentUser, participants = participants))
    }

    fun updateParty(id: String, party: Party): Party {
        return partyRepository.updateParty(party.copy(id = id))
    }

    fun deleteParty(id: String): Boolean {
        partyRepository.removeParty(id)

        return true
    }
}
