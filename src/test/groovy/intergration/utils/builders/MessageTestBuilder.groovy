package intergration.utils.builders

import com.example.graphql.adapters.pgsql.message.*
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPayment
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class MessageTestBuilder {

    private static def defaults = [
            text       : 42.11,
            createdAt  : ZonedDateTime.now(),
            user       : null,
            party      : null,
            payment    : null,
            bulkPayment: null,
            expense    : null
    ]

    private PersistentMessageTestBuilder() {}

    private static assignMessageArgs(PersistentMessage message, Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args

        message.text = (allArgs.text as Long)
        message.createdAt = (allArgs.createdAt as ZonedDateTime)
        message.user = (allArgs.user as PersistentUser)
    }

    static PersistentPartyMessage aPartyMessage(Map props = [:], PersistentPartyMessageRepository repository) {
        def message = new PersistentPartyMessage(props.party as PersistentParty)
        assignMessageArgs(message, props)

        return repository.save(message)
    }

    static PersistentPaymentMessage aPaymentMessage(Map props = [:], PersistentPaymentMessageRepository repository) {
        def message = new PersistentPaymentMessage(props.payment as PersistentPayment)
        assignMessageArgs(message, props)

        return repository.save(message)
    }

    static PersistentBulkPaymentMessage aBulkPaymentMessage(Map props = [:], PersistentBulkPaymentMessageRepository repository) {
        def message = new PersistentBulkPaymentMessage(props.bulkPayment as PersistentBulkPayment)
        assignMessageArgs(message, props)

        return repository.save(message)
    }

    static PersistentExpenseMessage aExpenseMessage(Map props = [:], PersistentExpenseMessageRepository repository) {
        def message = new PersistentExpenseMessage(props.expense as PersistentExpense)
        assignMessageArgs(message, props)

        return repository.save(message)
    }
}
