package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User
import com.expediagroup.graphql.annotations.GraphQLID

data class PartyRequest(
        @GraphQLID
        val id: String,
        val user: User? = null,
        val party: Party? = null,
        val status: PartyRequestStatus = PartyRequestStatus.IN_PROGRESS
)

enum class PartyRequestStatus {
    ACCEPTED,
    DECLINED,
    IN_PROGRESS,
}
