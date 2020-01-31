package com.example.graphql.resolvers.partyrequest

import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.expediagroup.graphql.annotations.GraphQLID

data class PartyRequestType(
        @GraphQLID
        val id: String,
        val status: PartyRequestStatus
)


fun PartyRequest.toResponse() = PartyRequestType(id = this.id, status = this.status)
