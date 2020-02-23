package com.example.graphql.resolvers.user

import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.payment.PaymentType
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID
import javax.validation.constraints.Email

data class UserType(
        @GraphQLID
        override val id: String = "0",

        @field:Email
        val email: String = "",

        val name: String? = null,

        val bankAccount: String? = null
) : GQLResponseType {

    lateinit var userPartyRequests: List<PartyRequestType>

    lateinit var userJoinedParties: List<PartyType>

    lateinit var userPayments: List<PaymentType>

    lateinit var userExpenses: List<ExpenseType>

//    lateinit var  messageGroups: List<MessageGroup>
}

fun User.toResponse() = UserType(
        id = this.id.toString(),
        name = this.name,
        email = this.email,
        bankAccount = this.bankAccount
)
