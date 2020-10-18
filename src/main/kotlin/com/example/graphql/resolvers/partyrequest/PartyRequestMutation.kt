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
            partyRequestId: String,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.acceptRequest(partyRequestId.toLong(), context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun declinePartyRequest(
            partyRequestId: String,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.declineRequest(partyRequestId.toLong(), context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun removePartyRequest(
            partyRequestId: String,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.removePartyRequest(partyRequestId.toLong(), context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun sendPartyRequest(
            partyId: String,
            requestReceiverId: String,
            @GraphQLContext context: AppGraphQLContext
    ) = partyRequestService.sendPartyRequest(requestReceiverId.toLong(), partyId.toLong(), context.subject)?.toResponse() }
