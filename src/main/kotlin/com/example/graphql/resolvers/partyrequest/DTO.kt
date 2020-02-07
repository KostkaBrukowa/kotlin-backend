package com.example.graphql.resolvers.partyrequest

import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID

data class PartyRequestType(
        @GraphQLID
        override val id: String,

        val status: PartyRequestStatus
) : GQLResponseType {
        lateinit var partyRequestReceiver: UserType

        lateinit var partyRequestParty: PartyType
}


fun PartyRequest.toResponse() = PartyRequestType(id = this.id.toString(), status = this.status)
