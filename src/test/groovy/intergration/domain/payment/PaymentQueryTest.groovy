package intergration.domain.payment

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.message.PersistentBulkPaymentMessageRepository
import com.example.graphql.adapters.pgsql.message.PersistentPaymentMessageRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPaymentRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.payment.BulkPaymentStatus
import com.example.graphql.domain.payment.PaymentStatus
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import static intergration.utils.builders.MessageTestBuilder.aBulkPaymentMessage
import static intergration.utils.builders.MessageTestBuilder.aPaymentMessage
import static intergration.utils.builders.PersistentBulkPaymentTestBuilder.aBulkPayment
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

    @Autowired
    PersistentBulkPaymentRepository bulkPaymentRepository

    @Autowired
    PersistentPaymentMessageRepository paymentMessageRepository

    @Autowired
    PersistentBulkPaymentMessageRepository bulkPaymentMessageRepository

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
        def message = aPaymentMessage([user: baseUser, payment: payment], paymentMessageRepository)

        and:
        def getSinglePaymentQuery = ("""
            getSinglePayment(paymentId: "${payment.id}") { 
                id
                amount
                confirmImageUrl
                status
                paymentPayer { id }
                paymentExpense { id }
                paymentMessages { id, messageSender { id } }
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
        response.paymentMessages.size() == 1
        response.paymentMessages[0].id.toLong() == message.id
        response.paymentMessages[0].messageSender.id.toLong() == baseUser.id
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

    def "Should return correct bulk payments"() {
        given:
        authenticate()

        and:
        def client = aClient(userRepository)
        def bulkPayment = aBulkPayment([
                amount         : 44.0,
                confirmImageUrl: 'www.google.com',
                status         : BulkPaymentStatus.IN_PROGRESS,
                payer          : client,
                receiver       : baseUser
        ], bulkPaymentRepository)

        and:
        def payment = aPayment([
                expense      : anExpense([user: baseUser], expenseRepository),
                user         : client,
                bulkedPayment: bulkPayment
        ], paymentRepository)
        def message = aBulkPaymentMessage([user: baseUser, bulkPayment: bulkPayment], bulkPaymentMessageRepository)

        and:
        def getClientBulkPaymentsQuery = ({ String id ->
            """
            getClientBulkPayments(userId: "${id}") { 
                id
                amount 
                confirmImageUrl
                status
                bulkPaymentPayer { id }
                bulkPaymentReceiver { id }
                bulkPaymentPayments { id }
                bulkPaymentMessages { id, messageSender { id }}
            }
        """
        })

        when:
        def baseUserResponse = postQuery(getClientBulkPaymentsQuery(baseUser.id.toString()))
        def secondUserResponse = postQuery(getClientBulkPaymentsQuery(client.id.toString()))

        then:
        baseUserResponse.size() == 1
        baseUserResponse[0].amount.toFloat() == 44.0f
        baseUserResponse[0].status == 'IN_PROGRESS'
        baseUserResponse[0].bulkPaymentPayer.id.toLong() == client.id
        baseUserResponse[0].bulkPaymentReceiver.id.toLong() == baseUser.id
        baseUserResponse[0].bulkPaymentPayments.size() == 1
        baseUserResponse[0].bulkPaymentPayments[0].id.toLong() == payment.id
        baseUserResponse[0].bulkPaymentMessages.size() == 1
        baseUserResponse[0].bulkPaymentMessages[0].id.toLong() == message.id
        baseUserResponse[0].bulkPaymentMessages[0].messageSender.id.toLong() == baseUser.id

        secondUserResponse.size() == 1
        secondUserResponse[0].amount.toFloat() == 44.0f
        secondUserResponse[0].status == 'IN_PROGRESS'
        secondUserResponse[0].bulkPaymentPayer.id.toLong() == client.id
        secondUserResponse[0].bulkPaymentReceiver.id.toLong() == baseUser.id
        secondUserResponse[0].bulkPaymentPayments.size() == 1
        secondUserResponse[0].bulkPaymentPayments[0].id.toLong() == payment.id
        secondUserResponse[0].bulkPaymentMessages.size() == 1
        secondUserResponse[0].bulkPaymentMessages[0].id.toLong() == message.id
        secondUserResponse[0].bulkPaymentMessages[0].messageSender.id.toLong() == baseUser.id
    }
}
