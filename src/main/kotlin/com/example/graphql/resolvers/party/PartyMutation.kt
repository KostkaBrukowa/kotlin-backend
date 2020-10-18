package com.example.graphql.resolvers.party

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.party.PartyService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Validated
@Component
class PartyMutation(
        private val partyService: PartyService
) : Mutation {

    @Authenticated(role = Roles.USER)
    fun createParty(
            @Valid newPartyInput: NewPartyInput,
            @GraphQLContext context: AppGraphQLContext
    ) = partyService.createParty(newPartyInput.toDomain(), context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun updateParty(
            @Valid editPartyInput: EditPartyInput
    ) = partyService.updateParty(editPartyInput.toDomain()).toResponse()

    @Authenticated(role = Roles.USER)
    fun removeParty(
            id: String,
            @GraphQLContext context: AppGraphQLContext
    ) = partyService.deleteParty(id.toLong(), context.subject)?.toResponse()

    @Authenticated(role = Roles.USER)
    fun removeParticipant(
            partyId: String,
            participantId: String,
            @GraphQLContext context: AppGraphQLContext
    ) = partyService.removeParticipant(partyId.toLong(), participantId.toLong(), context.subject)?.toResponse()
}
