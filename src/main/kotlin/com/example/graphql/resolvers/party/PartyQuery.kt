package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyService
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.UserService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class PartyQuery(
        private val partyService: PartyService,
        private val userService: UserService,
        private val partyRequestService: PartyRequestService
) : Query {

    @Authenticated(role = Roles.USER)
    fun getAllParties(userId: String): List<PartyType> =
            partyService.getAllParties(userId).map { it.toResponse() }

    @Authenticated(role = Roles.USER)
    fun getSingleParty(partyId: String): Party? = partyService.getSingleParty(partyId)

    @Authenticated(role = Roles.USER)
    fun getPartyParticipants(partyId: String) = userService.getAllPartyParticipants(partyId)
}
