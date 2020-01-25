package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class PartyQuery(private val partyService: PartyService): Query {

    @Authenticated(role = Roles.USER)
    fun getAllParties(userId: String): List<Party> = partyService.getAllParties(userId)

    @Authenticated(role = Roles.USER)
    fun getSingleParty(partyId: String): Party = partyService.getSingleParty(partyId)
}
