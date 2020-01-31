package com.example.graphql.domain.user

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.partyrequest.PartyRequest

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class UserTestBuilder {

    private static def defaults = [
            id              : '0',
            email           : 'test@email.com',
            partyRequests   : [],
            joinedParties   : [],
            expenses        : [],
            messageGroups   : [],
            name            : 'testname',
            bankAccount     : '3921321938',
            password        : '@fdaksl228@*##8',
            isEmailConfirmed: true,
    ]


    private UserTestBuilder() {}

    static User defaultUser(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new User(
                allArgs.id as String,
                allArgs.email as String,
                allArgs.partyRequests as List<PartyRequest>,
                allArgs.joinedParties as List<Party>,
                allArgs.expenses as List<Expense>,
                allArgs.messageGroups as List<MessageGroup>,
                allArgs.name as String,
                allArgs.bankAccount as String,
                allArgs.password as String,
                allArgs.isEmailConfirmed as Boolean,
        )
    }
}
