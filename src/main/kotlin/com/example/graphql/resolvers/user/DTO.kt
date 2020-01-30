package com.example.graphql.resolvers.user

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import com.expediagroup.graphql.annotations.GraphQLID

data class UserType(
        @GraphQLID
        val id: String = "0",

        val email: String = "",

        val name: String? = null,

        val bankAccount: String? = null,

        private val partyRequestService: PartyRequestService
) {

    fun partyRequests(): List<PartyRequestType> = partyRequestService.getAllPartyRequestsByUserId(this.id).map { it.toResponse() }

    fun expenses(): List<Expense> = emptyList()

    fun messageGroups(): List<MessageGroup> = emptyList()
}

fun User.toResponse(partyRequestService: PartyRequestService) = UserType(
        id = this.id,
        name = this.name,
        email = this.email,
        bankAccount = this.bankAccount,
        partyRequestService = partyRequestService
)
