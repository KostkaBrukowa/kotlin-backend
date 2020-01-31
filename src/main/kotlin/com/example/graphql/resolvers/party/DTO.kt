package com.example.graphql.resolvers.party

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime

data class PartyType(
        @GraphQLID
        val id: String = "0",

        val name: String,

        val owner: User? = null,

        val description: String? = null,

        val startDate: ZonedDateTime,

        val endDate: ZonedDateTime? = null

) {

    lateinit var partyParticipants: List<UserType>

    lateinit var partyPartyRequests: List<PartyRequestType>

    fun expenses(): List<Expense> = emptyList()
}

fun Party.toResponse() = PartyType(
        id = this.id,
        name = this.name,
        owner = this.owner,
        description = this.description,
        startDate = this.startDate,
        endDate = this.endDate
)
