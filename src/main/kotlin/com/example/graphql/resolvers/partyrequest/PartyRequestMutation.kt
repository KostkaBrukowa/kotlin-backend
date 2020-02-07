package com.example.graphql.resolvers.partyrequest

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component

@Component
class PartyRequestMutation(private val partyRequestService: PartyRequestService) : Mutation {

    @Authenticated(role = Roles.USER)
    fun acceptPartyRequest(
            partyRequestId: Long,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.acceptRequest(partyRequestId, context.subject)

    @Authenticated(role = Roles.USER)
    fun declinePartyRequest(
            partyRequestId: Long,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.declineRequest(partyRequestId, context.subject)

    @Authenticated(role = Roles.USER)
    fun removePartyRequest(
            partyRequestId: Long,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.removePartyRequest(partyRequestId, context.subject)

    @Authenticated(role = Roles.USER)
    fun sendPartyRequest(
            partyId: Long,
            requestReceiverId: Long,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.sendPartyRequest(requestReceiverId, partyId, context.subject)?.toResponse()
}
