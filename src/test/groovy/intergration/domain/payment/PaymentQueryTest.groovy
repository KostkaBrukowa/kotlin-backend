package intergration.domain.payment

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.payment.PaymentStatus
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class PaymentQueryTest extends BaseIntegrationSpec {
    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentExpenseRepository expenseRepository

    @Autowired
    PersistentPaymentRepository paymentRepository

    def "Should return single payment"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)
        def aExpense = anExpense([party: aParty, user: aClient(userRepository)], expenseRepository)
        def payment = aPayment([
                user           : baseUser,
                expense        : aExpense,
                amount         : 42.42,
                status         : PaymentStatus.PAID,
                confirmImageUrl: 'www.google.com'
        ], paymentRepository)

        and:
        def getSinglePaymentQuery = ("""
            getSinglePayment(paymentId: "${payment.id}") { 
                id
                amount
                confirmImageUrl
                status
                paymentPayer { id }
                paymentExpense { id }
            }
        """)

        when:
        def response = postQuery(getSinglePaymentQuery)

        then:
        response.id.toLong() == payment.id
        response.amount.toFloat() == 42.42f
        response.confirmImageUrl == 'www.google.com'
        response.status == 'PAID'
        response.paymentPayer.id.toLong() == baseUser.id
        response.paymentExpense.id.toLong() == aExpense.id
    }

    def "Should return user's payments"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)
        def aExpense = anExpense([party: aParty, user: aClient(userRepository)], expenseRepository)
        def payment1 = aPayment([user: baseUser, expense: aExpense], paymentRepository)
        def payment2 = aPayment([user: baseUser, expense: aExpense], paymentRepository)
        def payment3 = aPayment([user: baseUser, expense: aExpense], paymentRepository)

        and:
        def getClientsPaymentsQuery = ("""
            getClientsPayments(userId: "${baseUser.id}") { id }
        """)

        when:
        def response = postQuery(getClientsPaymentsQuery)

        then:
        response.size() == 3
        response.any { it.id.toLong() == payment1.id }
        response.any { it.id.toLong() == payment2.id }
        response.any { it.id.toLong() == payment3.id }
    }
}
