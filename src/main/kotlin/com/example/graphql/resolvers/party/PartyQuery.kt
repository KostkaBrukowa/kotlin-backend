package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyService
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class PartyQuery(private val partyService: PartyService): Query {

    fun getTestParty(): Party {
        return partyService.getTestParty()
    }
}
