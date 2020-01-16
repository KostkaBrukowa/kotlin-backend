package com.example.graphql.domain.party

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.partyrequest.PartyRequest
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime

data class Party(
        @GraphQLID
        val id: String = "0",
        val messageGroup: MessageGroup? = null,
        val partyRequests: List<PartyRequest> = emptyList(),
        val expenses: List<Expense> = emptyList(),
        val name: String,
        val description: String = "",
        val startDate: ZonedDateTime,
        val endDate: ZonedDateTime? = null
)
