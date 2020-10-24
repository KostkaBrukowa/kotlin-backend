package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyKind
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.message.MessageResponseType
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID
import org.hibernate.validator.constraints.Length
import java.time.ZonedDateTime
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.Max
import javax.validation.constraints.Min

data class PartyType(
        @GraphQLID
        override val id: String = "0",

        val name: String? = null,
        val owner: UserType? = null,
        val description: String? = null,
        val startDate: ZonedDateTime?,
        val endDate: ZonedDateTime? = null,
        val locationName: String? = null,
        val locationLatitude: Float? = null,
        val locationLongitude: Float? = null,
        val type: PartyKind

) : GQLResponseType {

    lateinit var partyParticipants: List<UserType>

    lateinit var partyPartyRequests: List<PartyRequestType>

    lateinit var partyExpenses: List<ExpenseType>

    lateinit var partyMessages: List<MessageResponseType>
}

fun Party.toResponse() = PartyType(
        id = this.id.toString(),
        name = this.name,
        owner = this.owner?.toResponse(),
        description = this.description,
        startDate = this.startDate,
        endDate = this.endDate,
        locationName = this.locationName,
        locationLatitude = this.locationLatitude,
        locationLongitude = this.locationLongitude,
        type = this.type
)


data class NewPartyInput(
        @field:Length(min = 3, max = 256)
        val name: String,

        @field:FutureOrPresent
        val startDate: ZonedDateTime,

        @field:FutureOrPresent
        val endDate: ZonedDateTime?,

        val description: String?,

        val participants: List<Long>?,

        val locationName: String? = null,

        @Min(value = -90, message = "Latitude must me between -90 and 90")
        @Max(value = 90, message = "Latitude must me between -90 and 90")
        val locationLatitude: Float? = null,

        @Min(value = -180, message = "Longitude must me between -180 and 180")
        @Max(value = 180, message = "Longitude must me between -180 and 180")
        val locationLongitude: Float? = null,

        val type: PartyKind
) {

    fun toDomain(): Party = Party(
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            participants = this.participants?.map { it -> User(id = it) } ?: emptyList(),
            locationName = this.locationName,
            locationLatitude = this.locationLatitude,
            locationLongitude = this.locationLongitude,
            type = type
    )
}

data class EditPartyInput(
        val id: String,

        @field:Length(min = 3, max = 256)
        val name: String?,

        @field:FutureOrPresent
        val startDate: ZonedDateTime?,

        @field:FutureOrPresent
        val endDate: ZonedDateTime?,

        val description: String?,

        val locationName: String? = null,

        @Min(value = -90, message = "Latitude must me between -90 and 90")
        @Max(value = 90, message = "Latitude must me between -90 and 90")
        val locationLatitude: Float? = null,

        @Min(value = -180, message = "Longitude must me between -180 and 180")
        @Max(value = 180, message = "Longitude must me between -180 and 180")
        val locationLongitude: Float? = null,

        val type: PartyKind
) {

    fun toDomain(): Party = Party(
            id = this.id.toLong(),
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            locationName = this.locationName,
            locationLatitude = this.locationLatitude,
            locationLongitude = this.locationLongitude,
            type = this.type
    )
}
