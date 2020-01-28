package com.example.graphql.domain.party

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.user.User

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PartyTestBuilder {

    private static def defaults = [
            id           : '0',
            name         : 'testname',
            owner        : null,
            messageGroup : null,
            participants : [],
            partyRequests: [],
            expenses     : [],
            description  : 'test description',
            startDate    : "2020-01-27T12:33:39.536632+01:00[Europe/Warsaw]",
            endDate      : "2020-01-27T12:33:39.536632+01:00[Europe/Warsaw]",
    ]

    private UserTestBuilder() {}

    static Party defaultParty(Map args = [:]) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new Party(
                allArgs.id as String,
                allArgs.name as String,
                allArgs.owner as User,
                allArgs.messageGroup as MessageGroup,
                allArgs.participants as List<User>,
                allArgs.partyRequests as List<PartyRequest>,
                allArgs.expenses as List<Expense>,
                allArgs.description,
                ZonedDateTime.parse(allArgs.startDate),
                ZonedDateTime.parse(allArgs.endDate),
        )
    }
}
