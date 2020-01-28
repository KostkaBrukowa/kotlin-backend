package com.example.graphql.resolvers.party

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserService
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime

data class PartyType(
        @GraphQLID
        val id: String = "0",

        val name: String,

        val owner: User? = null,

        val description: String? = null,

        val startDate: ZonedDateTime,

        val endDate: ZonedDateTime? = null,

        private val userService: UserService
) {

    fun participants(): List<User> {
        return userService.getAllPartyParticipants(this.id)
    }

    fun messageGroup(): MessageGroup? = null

    fun partyRequests(): List<PartyRequest> = emptyList()

    fun expenses(): List<Expense> = emptyList()
}

fun Party.toResponse(userService: UserService) = PartyType(
        id = this.id,
        name = this.name,
        owner = this.owner,
        description = this.description,
        startDate = this.startDate,
        endDate = this.endDate,
        userService = userService
)
