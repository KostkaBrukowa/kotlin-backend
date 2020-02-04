package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User

data class PartyRequest(
        val id: Long,
        val status: PartyRequestStatus = PartyRequestStatus.IN_PROGRESS,


        val user: User? = null,
        val party: Party? = null
)

enum class PartyRequestStatus {
    ACCEPTED,
    DECLINED,
    IN_PROGRESS,
}
