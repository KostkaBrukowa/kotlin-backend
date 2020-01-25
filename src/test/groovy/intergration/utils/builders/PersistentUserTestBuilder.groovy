package intergration.utils.builders

import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.MessageGroup
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PersistentPartyRequest
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.User
import org.apache.commons.lang.RandomStringUtils

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PersistentUserTestBuilder {
    private static def defaults = [
            id              : '0',
            email           :  'test@email.com',
            partyRequests   : [],
            expenses        : [],
            messageGroups   : [],
            name            : 'testname',
            bankAccount     : '3921321938',
            password        : '@fdaksl228@*##8',
            isEmailConfirmed: true,
    ]


    private PersistentUserTestBuilder() {}

    static PersistentUser defaultPersistentUser(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PersistentUser(
                allArgs.id as Long,
                allArgs.partyRequests as List<PersistentPartyRequest>,
                allArgs.expenses as List<PersistentExpense>,
                allArgs.messageGroups as List<PersistentMessageGroup>,
                allArgs.name as String,
                allArgs.email as String,
                allArgs.bankAccount as String,
                allArgs.password as String,
                allArgs.isEmailConfirmed as Boolean,
        )
    }

    static PersistentUser aClientWithId(Long id, PersistentUserRepository repository) {
        return aClient(["id": id, "email": RandomStringUtils.random(5) + "@gmail.com"], repository)
    }

    static PersistentUser aClient(Map props = [:], PersistentUserRepository repository) {
        return repository.save(defaultPersistentUser(props))
    }
}
