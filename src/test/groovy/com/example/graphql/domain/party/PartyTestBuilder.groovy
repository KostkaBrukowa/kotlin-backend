package com.example.graphql.domain.party

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.user.User

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PartyTestBuilder {

    private static def defaults = [
            id               : 0,
            name             : 'party test name',
            owner            : null,
            participants     : [],
            partyRequests    : [],
            expenses         : [],
            description      : 'test description',
            startDate        : "2020-01-27T12:33:39.536632+01:00[Europe/Warsaw]",
            endDate          : "2020-01-27T12:33:39.536632+01:00[Europe/Warsaw]",
            locationName     : null,
            locationLatitude : null,
            locationLongitude: null,
            type             : PartyKind.EVENT,
    ]

    private UserTestBuilder() {}

    static Party defaultParty(Map args = [:]) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new Party(
                allArgs.id as Long,
                allArgs.name as String,
                allArgs.description as String,
                ZonedDateTime.parse(allArgs.startDate),
                ZonedDateTime.parse(allArgs.endDate),
                allArgs.locationName as String,
                allArgs.locationLatitude as Float,
                allArgs.locationLongitude as Float,
                allArgs.type as PartyKind,
                allArgs.owner as User,
                allArgs.participants as List<User>,
                allArgs.partyRequests as List<PartyRequest>,
                allArgs.expenses as List<Expense>,
        )
    }
}
