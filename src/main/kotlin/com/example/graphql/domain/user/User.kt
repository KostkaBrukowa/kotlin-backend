package com.example.graphql.domain.user

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.partyrequest.PartyRequest
import com.expediagroup.graphql.annotations.GraphQLID
import com.expediagroup.graphql.annotations.GraphQLIgnore

data class User(
        @GraphQLID
        val id: String = "0",

        val email: String = "",

        val partyRequests: List<PartyRequest> = emptyList(),

        val expenses: List<Expense> = emptyList(),

        val messageGroups: List<MessageGroup> = emptyList(),

        val name: String? = null,

        val bankAccount: String? = null,

        @GraphQLIgnore
        val password: String = "",

        @GraphQLIgnore
        val isEmailConfirmed: Boolean = false
)
