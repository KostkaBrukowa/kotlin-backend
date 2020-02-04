package com.example.graphql.domain.party

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.user.User
import java.time.ZonedDateTime

data class Party(
        val id: Long = 0,

        val name: String = "",

        val description: String? = null,

        val startDate: ZonedDateTime = ZonedDateTime.now(),

        val endDate: ZonedDateTime? = null,


        val owner: User? = null,

        val messageGroup: MessageGroup? = null,

        val participants: List<User> = emptyList(),

        val partyRequests: List<PartyRequest> = emptyList(),

        val expenses: List<Expense> = emptyList()
)
