package com.example.graphql.domain.user

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.payment.Payment
import com.expediagroup.graphql.annotations.GraphQLIgnore

data class User(
        val id: Long = 0,

        val email: String = "",

        val name: String? = null,

        val bankAccount: String? = null,

        @GraphQLIgnore
        val password: String = "",

        @GraphQLIgnore
        val isEmailConfirmed: Boolean = false,


        val partyRequests: List<PartyRequest> = emptyList(),

        val joinedParties: List<Party> = emptyList(),

        val expenses: List<Expense> = emptyList(),

        val payments: List<Payment> = emptyList(),

        val messageGroups: List<MessageGroup> = emptyList()
)
