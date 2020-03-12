package intergration.utils.builders

import com.example.graphql.adapters.pgsql.notification.PersistentNotification
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser
import org.apache.commons.lang.RandomStringUtils
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PersistentUserTestBuilder {
    private static def defaults = [
            id                  : '0',
            email               : 'test@email.com',
            partyRequests       : [],
            expenses            : [],
            payments            : [],
            messages            : [],
            createdNotifications: [],
            notifications       : [],
            ownedParties        : [],
            joinedParties       : [],
            friends             : [],
            friendOf            : [],
            name                : 'persistent user test name',
            bankAccount         : '3921321938',
            password            : '@fdaksl228@*##8',
            isEmailConfirmed    : true,
    ]


    private PersistentUserTestBuilder() {}

    static PersistentUser defaultPersistentUser(Map args = [:]) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PersistentUser(
                allArgs.id as Long,
                allArgs.name as String,
                allArgs.email as String,
                allArgs.bankAccount as String,
                allArgs.password as String,
                allArgs.isEmailConfirmed as Boolean,
                allArgs.partyRequests as List<PersistentPartyRequest>,
                allArgs.ownedParties as List<PersistentParty>,
                allArgs.expenses as Set<PersistentExpense>,
                allArgs.payments as Set<PersistentPayment>,
                allArgs.messages as Set<PersistentMessage>,
                allArgs.createdNotifications as Set<PersistentNotification>,
                allArgs.notifications as Set<PersistentNotification>,
                allArgs.joinedParties as Set<PersistentParty>,
                allArgs.friends as Set<PersistentUser>,
                allArgs.friendOf as Set<PersistentUser>,
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    static PersistentUser aClient(Map props = [:], PersistentUserRepository repository) {
        if (!props.containsKey("email")) {
            props.email = RandomStringUtils.random(5) + "@gmail.com"
        }

        return repository.save(defaultPersistentUser(props))
    }
}
