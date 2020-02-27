package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.message.MessageResponseType
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID
import org.hibernate.validator.constraints.Length
import java.time.ZonedDateTime
import javax.validation.constraints.FutureOrPresent

data class PartyType(
        @GraphQLID
        override val id: String = "0",

        val name: String,

        val owner: User? = null,

        val description: String? = null,

        val startDate: ZonedDateTime,

        val endDate: ZonedDateTime? = null

): GQLResponseType {

    lateinit var partyParticipants: List<UserType>

    lateinit var partyPartyRequests: List<PartyRequestType>

    lateinit var partyExpenses: List<ExpenseType>

    lateinit var partyMessages: List<MessageResponseType>
}

fun Party.toResponse() = PartyType(
        id = this.id.toString(),
        name = this.name,
        owner = this.owner,
        description = this.description,
        startDate = this.startDate,
        endDate = this.endDate
)


data class NewPartyInput(
        @field:Length(min = 3, max = 256)
        val name: String,

        @field:FutureOrPresent
        val startDate: ZonedDateTime,

        @field:FutureOrPresent
        val endDate: ZonedDateTime?,

        val description: String?,

        val participants: List<Long>?
) {

    fun toDomain(): Party = Party(
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            participants = this.participants?.map { it -> User(id = it) } ?: emptyList()
    )
}

data class EditPartyInput(
        @field:Length(min = 3, max = 256)
        val name: String,

        @field:FutureOrPresent
        val startDate: ZonedDateTime,

        @field:FutureOrPresent
        val endDate: ZonedDateTime?,

        val description: String?
) {

    fun toDomain(): Party = Party(
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate
    )
}
