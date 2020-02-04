package intergration.utils.builders

import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.domain.user.PersistentUser

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PersistentPartyTestBuilder {

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

    private PersistentPartyTestBuilder() {}

    static PersistentParty defaultPersistentParty(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PersistentParty(
                allArgs.id as Long,
                allArgs.name as String,
                allArgs.description as String,
                allArgs.startDate instanceof ZonedDateTime ? allArgs.startDate : ZonedDateTime.parse(allArgs.startDate) as ZonedDateTime,
                allArgs.endDate instanceof ZonedDateTime ? allArgs.endDate : ZonedDateTime.parse(allArgs.endDate) as ZonedDateTime,
                allArgs.owner as PersistentUser,
                allArgs.messageGroup as PersistentMessageGroup,
                allArgs.participants as List<PersistentUser>,
                allArgs.partyRequests as List<PersistentPartyRequest>,
                allArgs.expenses as List<PersistentExpense>,
        )
    }

    static PersistentParty aParty(Map props = [:], PersistentPartyRepository repository) {
        return repository.save(defaultPersistentParty(props))
    }
}
