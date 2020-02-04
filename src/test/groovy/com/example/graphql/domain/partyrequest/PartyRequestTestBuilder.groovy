package com.example.graphql.domain.partyrequest

import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyTestBuilder
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserTestBuilder
import intergration.domain.party.PartyTest

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PartyRequestTestBuilder {

    private static def defaults = [
            id    : 0,
            user  : UserTestBuilder.defaultUser(),
            party : PartyTestBuilder.defaultParty(),
            status: PartyRequestStatus.IN_PROGRESS,
    ]

    private PartyRequestTestBuilder() {}

    static PartyRequest defaultPartyRequest(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PartyRequest(
                allArgs.id as Long,
                allArgs.status as PartyRequestStatus,
                allArgs.user as User,
                allArgs.party as Party,
        )
    }
}

