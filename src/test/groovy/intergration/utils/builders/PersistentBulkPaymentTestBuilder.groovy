package intergration.utils.builders

import com.example.graphql.adapters.pgsql.message.PersistentBulkPaymentMessage
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPayment
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPaymentRepository
import com.example.graphql.domain.payment.BulkPaymentStatus
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PersistentBulkPaymentTestBuilder {

    private static def defaults = [
            id             : 0,
            amount         : 42.11,
            confirmImageUrl: 'www.google.com',
            status         : BulkPaymentStatus.IN_PROGRESS,
            payer          : null,
            receiver       : null,
            payments       : [],
            messages       : [],
    ]

    private PersistentBulkPaymentTestBuilder() {}

    static PersistentBulkPayment defaultPersistentBulkPayment(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PersistentBulkPayment(
                allArgs.id as Long,
                allArgs.amount as Float,
                allArgs.confirmImageUrl as String,
                allArgs.status as BulkPaymentStatus,
                allArgs.payer as PersistentUser,
                allArgs.receiver as PersistentUser,
                allArgs.payments as Set<PersistentPayment>,
                allArgs.messages as Set<PersistentBulkPaymentMessage>,
        )
    }

    static PersistentBulkPayment aBulkPayment(Map props = [:], PersistentBulkPaymentRepository repository) {
        return repository.save(defaultPersistentBulkPayment(props))
    }
}
