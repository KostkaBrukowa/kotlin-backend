package intergration.domain.payment

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.payment.PaymentStatus
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

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

    @Unroll
    def "Should #shouldChange change payment status when status is changed from #statusFrom to #statusTo"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)
        def aExpense = anExpense([party: aParty(partyRepository), user: aClient(userRepository)], expenseRepository)
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
        def response = postMutation(updatePaymentStatusMutation)

        def actualExpense = paymentRepository.findById(payment.id).get()

        then:
        if (shouldChange) {
            assert actualExpense.paymentStatus == statusTo
        } else {
            assert response[0].errorType == 'ValidationError'
        }

        where:
        statusFrom                | statusTo                | shouldChange
        PaymentStatus.IN_PROGRESS | PaymentStatus.ACCEPTED  | true
        PaymentStatus.IN_PROGRESS | PaymentStatus.CONFIRMED | false
    }

    @Unroll
    def "Only expense owner can confirm a payment"() {
        given:
        authenticate()

        and:
        def theExpenseOwner = expenseOwner == 'loggedUser' ? baseUser : aClient(userRepository)
        def aParty = aParty([owner: baseUser], partyRepository)
        def aExpense = anExpense([party: aParty(partyRepository), user: theExpenseOwner], expenseRepository)
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
        def response = postMutation(updatePaymentStatusMutation)

        and:
        def actualExpense = paymentRepository.findById(payment.id).get()

        then:
        if (shouldChange) {
            assert actualExpense.paymentStatus == PaymentStatus.CONFIRMED
        } else {
            assert response[0].errorType == 'ValidationError'
            assert response[0].message.contains('User is not authorised to perform this action')
        }

        where:
        expenseOwner | paymentPayer | shouldChange
        'loggedUser' | 'otherUser'  | true
        'otherUser'  | 'loggedUser' | false
        'otherUser'  | 'otherUser'  | false
    }
}
