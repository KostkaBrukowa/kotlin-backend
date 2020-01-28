package com.example.graphql.resolvers.party

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyService
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.hibernate.validator.constraints.Length
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.time.ZonedDateTime
import javax.validation.Valid
import javax.validation.constraints.FutureOrPresent

@Validated
@Component
class PartyMutation(private val partyService: PartyService, private val userService: UserService) : Mutation {

    @Authenticated(role = Roles.USER)
    fun createParty(
            @Valid newPartyInput: NewPartyInput,
            @GraphQLContext context: AppGraphQLContext
    ) = partyService.createParty(newPartyInput.toDomain(), context.subject).toResponse(userService)

    @Authenticated(role = Roles.USER)
    fun updateParty(id: String, @Valid newPartyInput: NewPartyInput) = partyService.updateParty(id, newPartyInput.toDomain())

    @Authenticated(role = Roles.USER)
    fun deleteParty(id: String) = partyService.deleteParty(id)
}

data class NewPartyInput(
        @field:Length(min = 3, max = 256)
        val name: String,

        @field:FutureOrPresent
        val startDate: ZonedDateTime,

        @field:FutureOrPresent
        val endDate: ZonedDateTime?,

        val description: String?,

        val participants: List<String>?
) {

    fun toDomain(): Party = Party(
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate,
            participants = this.participants?.map { it -> User(id = it) } ?: emptyList()
    )
}
