package intergration.domain.expense

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.message.PersistentExpenseMessageRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import java.time.ZonedDateTime

import static intergration.utils.builders.MessageTestBuilder.aExpenseMessage
import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class ExpenseQueryTest extends BaseIntegrationSpec {
    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentExpenseRepository expenseRepository

    @Autowired
    PersistentPaymentRepository paymentRepository

    @Autowired
    PersistentExpenseMessageRepository messageRepository

    def "Should return single expense"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)
        def threeDaysEarlier = ZonedDateTime.now().minusDays(3)
        def expense = anExpense([
                user       : baseUser,
                party      : aParty,
                amount     : 42.43,
                expenseDate: threeDaysEarlier,
                description: "test expense description"
        ], expenseRepository)
        def expensePayment1 = aPayment([user: baseUser, expense: expense], paymentRepository)
        def expensePayment2 = aPayment([user: aClient(userRepository), expense: expense], paymentRepository)
        def expenseMessage = aExpenseMessage([user: baseUser, expense: expense], messageRepository)

        and:
        def getSingleExpenseQuery = ("""
            getSingleExpense(expenseId: "${expense.id}") { 
                id
                amount
                expenseDate
                description
                expenseStatus
                expensePayer { id }
                expenseParty { id }
                expensePayments { id }
                expenseMessages { id }
            }
        """)

        when:
        def response = postQuery(getSingleExpenseQuery)

        then:
        response.id.toLong() == expense.id
        response.amount == 42.43
        response.expenseDate == threeDaysEarlier.toString()
        response.description == "test expense description"
        response.expenseStatus == "IN_PROGRESS_REQUESTING"
        response.expensePayer.id.toLong() == baseUser.id
        response.expenseParty.id.toLong() == aParty.id
        response.expensePayments.size() == 2
        response.expensePayments.any { it.id.toLong() == expensePayment1.id }
        response.expensePayments.any { it.id.toLong() == expensePayment2.id }
        response.expenseMessages.size() == 1
        response.expenseMessages[0].id.toLong() == expenseMessage.id
    }

    def "Should return all expenses for an user"() {
        given:
        authenticate()

        and:
        def firstExpense = anExpense([user: baseUser, party: aParty([owner: baseUser], partyRepository)], expenseRepository)
        def secondExpense = anExpense([user: baseUser, party: aParty([owner: baseUser], partyRepository)], expenseRepository)
        def thirdExpense = anExpense([user: baseUser, party: aParty([owner: baseUser], partyRepository)], expenseRepository)

        and:
        def getExpensesForUserQuery = ("""getExpensesForUser(userId: "${baseUser.id}") { id, expensePayer { id }, expensePayments { id } }""")

        when:
        def response = postQuery(getExpensesForUserQuery, "getExpensesForUser")

        then:
        response.size() == 3
        response.any { it.id.toLong() == firstExpense.id }
        response.any { it.id.toLong() == secondExpense.id }
        response.any { it.id.toLong() == thirdExpense.id }
    }

    def "Should return all expenses for a party"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)
        def firstExpense = anExpense([user: aClient(userRepository), party: aParty], expenseRepository)
        def secondExpense = anExpense([user: aClient(userRepository), party: aParty], expenseRepository)
        def thirdExpense = anExpense([user: aClient(userRepository), party: aParty], expenseRepository)

        and:
        def getExpensesForPartyQuery = ("""getExpensesForParty(partyId: "${aParty.id}") { id }""")

        when:
        def response = postQuery(getExpensesForPartyQuery, "getExpensesForParty")

        then:
        response.size() == 3
        response.any { it.id.toLong() == firstExpense.id }
        response.any { it.id.toLong() == secondExpense.id }
        response.any { it.id.toLong() == thirdExpense.id }

    }

    def "Should return an error when different user tries to read someone else's expenses"() {
        given:
        authenticate()

        and:
        def getPartRequestsForPartyQuery = ("""getExpensesForUser(userId: "${aClient(userRepository).id}") { id }""")

        when:
        def response = postQuery(getPartRequestsForPartyQuery, "getPartyRequestsForUser", true)

        then:
        response[0].message.contains('User is not authorised to perform this action')
    }
}
