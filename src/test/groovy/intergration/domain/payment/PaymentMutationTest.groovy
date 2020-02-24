package intergration.domain.payment

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPaymentRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.payment.BulkPaymentStatus
import com.example.graphql.domain.payment.PaymentStatus
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import static intergration.utils.builders.PersistentBulkPaymentTestBuilder.aBulkPayment
import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class PaymentMutationTest extends BaseIntegrationSpec {
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

    @Unroll
    def "Should #shouldChange change payment status when status is changed from #statusFrom to #statusTo"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)
        def aExpense = anExpense([party: aParty, user: aClient(userRepository)], expenseRepository)
        def payment = aPayment([user: baseUser, expense: aExpense, status: statusFrom], paymentRepository)

        and:
        def updatePaymentStatusMutation = """
            updatePaymentStatus(
                updatePaymentStatusInput: {
                    paymentId: ${payment.id}
                    status: ${statusTo}
                }
            ) { id }
        """

        when:
        def response = postMutation(updatePaymentStatusMutation, null, !shouldChange)

        def actualExpense = paymentRepository.findById(payment.id).get()

        then:
        if (shouldChange) {
            assert actualExpense.paymentStatus == statusTo
        } else {
            assert response[0].errorType == 'ValidationError'
        }

        where:
        statusFrom                | statusTo               | shouldChange
        PaymentStatus.IN_PROGRESS | PaymentStatus.ACCEPTED | true
        PaymentStatus.IN_PROGRESS | PaymentStatus.PAID     | false
    }

    def "Should bulk payments with correct amount"() {
        given:
        authenticate()

        and:
        def client = aClient(userRepository)
        def expense = anExpense([user: baseUser], expenseRepository)

        and:
        def payment1 = aPayment([amount: 10.0, expense: expense, user: client], paymentRepository)
        def payment2 = aPayment([amount: 20.0, expense: expense, user: client], paymentRepository)
        def payment3 = aPayment([amount: 30.0, expense: expense, user: baseUser], paymentRepository)
        def payment4 = aPayment([amount: 40.0, expense: expense, user: baseUser], paymentRepository)

        and:
        def bulkPaymentsMutation = ({ String id ->
            """
            bulkPayments(
                paymentsIds: [
                    ${payment1.id}
                    ${payment2.id}
                    ${payment3.id}
                    ${payment4.id}
                ]
            ) { id }
        """
        })

        when:
        def bulkPaymentId = postMutation(bulkPaymentsMutation(baseUser.id.toString())).id.toLong()

        and:
        def bulkPayments = jdbcTemplate.queryForList("""
                SELECT * FROM bulk_payments
                LEFT JOIN payments ON bulk_payments.id = payments.bulked_payment_id
                WHERE bulk_payments.id = ${bulkPaymentId}
        """)

        and:
        def bulkPayment = bulkPayments[0]

        then:
        bulkPayment.amount == 40
        bulkPayment.payer_id.toLong() == baseUser.id
        bulkPayment.receiver_id.toLong() == client.id
        bulkPayments.every { it['bulked_payment_id'] == bulkPaymentId }
        bulkPayments.every { it['payment_status'] == 'BULKED' }
    }

    def "Should update bulk payment status"() {
        given:
        authenticate()

        and:
        def client = aClient(userRepository)
        def bulkPayment = aBulkPayment([
                status  : BulkPaymentStatus.IN_PROGRESS,
                payer   : baseUser,
                receiver: client
        ], bulkPaymentRepository)

        and:
        def updateBulkPaymentStatusMutation = """
            updateBulkPaymentStatus(
                updatePaymentStatusInput: {
                    id: ${bulkPayment.id}
                    status: 'PAID'
                }
            ) { id }
        """

        when:
        postMutation(updateBulkPaymentStatusMutation)

        and:
        def actualBulkPayment = bulkPaymentRepository.findById(bulkPayment.id).get()

        then:
        actualBulkPayment.status == BulkPaymentStatus.PAID
    }

    @Unroll
    def "Only expense owner can confirm a payment"() {
        given:
        authenticate()

        and:
        def theExpenseOwner = expenseOwner == 'loggedUser' ? baseUser : aClient(userRepository)
        def aParty = aParty([owner: baseUser], partyRepository)
        def aExpense = anExpense([party: aParty, user: theExpenseOwner], expenseRepository)
        def payment = aPayment([user: baseUser, expense: aExpense, status: PaymentStatus.PAID], paymentRepository)

        and:
        def updatePaymentStatusMutation = """
            updatePaymentStatus(
                updatePaymentStatusInput: {
                    paymentId: ${payment.id}
                    status: ${PaymentStatus.CONFIRMED}
                }
            ) { id }
        """

        when:
        def response = postMutation(updatePaymentStatusMutation, null, !shouldChange)

        and:
        def actualExpense = paymentRepository.findById(payment.id).get()

        then:
        if (shouldChange) {
            assert actualExpense.paymentStatus == PaymentStatus.CONFIRMED
        } else {
            assert response[0].errorType == 'DataFetchingException'
            assert response[0].message.contains('User is not authorised to perform this action')
        }

        where:
        expenseOwner | paymentPayer | shouldChange
        'loggedUser' | 'otherUser'  | true
        'otherUser'  | 'loggedUser' | false
        'otherUser'  | 'otherUser'  | false
    }
}
