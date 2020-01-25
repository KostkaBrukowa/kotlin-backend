package com.example.graphql.domain.party

import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class PartyService {
    fun getTestParty(): Party {
        return Party(
                name = "test name",
                startDate = ZonedDateTime.now()
        )
    }

    fun getAllParties(userId: String): List<Party> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getSingleParty(partyId: String): Party {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createParty(party: Party): Party {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun updateParty(id: String, party: Party): Party {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun deleteParty(id: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
