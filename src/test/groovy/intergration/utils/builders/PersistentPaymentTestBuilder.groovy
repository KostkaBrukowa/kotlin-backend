package intergration.utils.builders

import com.example.graphql.adapters.pgsql.message.PersistentPaymentMessage
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPayment
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PersistentPaymentTestBuilder {

    private static def defaults = [
            id             : 0,
            amount         : 42.11,
            confirmImageUrl: null,
            status         : PaymentStatus.IN_PROGRESS,
            expense        : null,
            user           : null,
            bulkedPayment  : null,
            messages       : [],
            createdAt      : ZonedDateTime.now(),
            paidAt         : null
    ]

    private PersistentPaymentTestBuilder() {}

    static PersistentPayment defaultPersistentPayment(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PersistentPayment(
                allArgs.id as Long,
                allArgs.createdAt as ZonedDateTime,
                allArgs.paidAt as ZonedDateTime,
                allArgs.amount as Float,
                allArgs.confirmImageUrl as String,
                allArgs.status as PaymentStatus,
                allArgs.expense as PersistentExpense,
                allArgs.user as PersistentUser,
                allArgs.bulkedPayment as PersistentBulkPayment,
                allArgs.messages as Set<PersistentPaymentMessage>,
        )
    }

    static PersistentPayment aPayment(Map props = [:], PersistentPaymentRepository repository) {
        return repository.save(defaultPersistentPayment(props))
    }
}
