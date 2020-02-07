package com.example.graphql.resolvers.partyrequest

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.stereotype.Component

@Component
class PartyRequestQuery(private val partyRequestService: PartyRequestService) : Query {

    @Authenticated(role = Roles.USER)
    fun getPartyRequestsForParty(partyId: Long) =
            partyRequestService.getAllPartyRequestsByPartyId(partyId).map { it.toResponse() }

    @Authenticated(role = Roles.USER)
    fun getPartyRequestsForUser(userId: Long, @GraphQLContext context: AppGraphQLContext) =
            partyRequestService.getAllPartyRequestsByUserId(userId, context.subject).map { it.toResponse() }
}
