package com.example.graphql.domain.user

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.partyrequest.PartyRequest
import com.expediagroup.graphql.annotations.GraphQLID
import com.expediagroup.graphql.annotations.GraphQLIgnore

data class User(
        @GraphQLID
        val id: String,

        val partyRequests: List<PartyRequest>,

        val expenses: List<Expense>,

        val messageGroups: List<MessageGroup>,

        val name: String?,

        val email: String,

        val bankAccount: String?,

        @GraphQLIgnore
        val isEmailConfirmed: Boolean = false
)
