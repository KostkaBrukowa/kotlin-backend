package intergration.utils.builders

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.message.PersistentExpenseMessage
import com.example.graphql.domain.expense.ExpenseStatus
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser

import java.time.ZonedDateTime

import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class PersistentExpenseTestBuilder {

    private static def defaults = [
            id           : 0,
            amount       : 42.42,
            expenseDate  : ZonedDateTime.now().minusDays(1),
            description  : "test expense description",
            expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING,
            user         : null,
            party        : null,
            payments     : [],
            messages     : [],
    ]

    private PersistentExpenseTestBuilder() {}

    static PersistentExpense defaultPersistentExpense(Map args) {
        verifyPropertyNames(defaults, args)

        def allArgs = defaults + args
        return new PersistentExpense(
                allArgs.id as Long,
                allArgs.amount as Float,
                allArgs.expenseDate as ZonedDateTime,
                allArgs.description as String,
                allArgs.expenseStatus as ExpenseStatus,
                allArgs.user as PersistentUser,
                allArgs.party as PersistentParty,
                allArgs.payments as List<PersistentPayment>,
                allArgs.messages as Set<PersistentExpenseMessage>,
        )
    }

    static PersistentExpense anExpense(Map props = [:], PersistentExpenseRepository repository) {
        return repository.save(defaultPersistentExpense(props))
    }
}
