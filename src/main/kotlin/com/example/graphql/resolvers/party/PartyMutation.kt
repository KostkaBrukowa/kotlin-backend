package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.time.ZonedDateTime
import javax.validation.Valid
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@Validated
@Component
class PartyMutation(private val partyService: PartyService) : Mutation {

    @Authenticated(role = Roles.USER)
    fun createParty(@Valid newPartyInput: NewPartyInput) = partyService.createParty(newPartyInput.toDomain())

    @Authenticated(role = Roles.USER)
    fun updateParty(id: String, @Valid newPartyInput: NewPartyInput) = partyService.updateParty(id, newPartyInput.toDomain())

    @Authenticated(role = Roles.USER)
    fun deleteParty(id: String) = partyService.deleteParty(id)
}

data class NewPartyInput(
        @field:Min(3)
        @field:Max(256)
        val name: String,

        @field:FutureOrPresent
        val startDate: ZonedDateTime,

        @field:FutureOrPresent
        val endDate: ZonedDateTime?,

        val description: String?
) {
    fun toDomain(): Party = Party(
            name= this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate
    )

    @AssertTrue
    private fun isValid() = startDate < endDate
}
