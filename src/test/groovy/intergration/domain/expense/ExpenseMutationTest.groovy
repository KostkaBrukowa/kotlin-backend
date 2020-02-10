package intergration.domain.expense

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Unroll

import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class ExpenseMutationTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentPartyRequestRepository partyRequestRepository

    @Autowired
    PersistentExpenseRepository expenseRepository

    @Autowired
    PersistentPaymentRepository paymentRepository

    def createExpenseMutation(Map props) {
        return """
            createExpense(
                newExpenseInput: {
                    amount: ${props.containsKey("amount") ? props.amount : "42,42"}
                    expenseDate: "${props.containsKey("expenseDate") ? props.expenseDate : ZonedDateTime.now().minusDays(1)}"
                    description: "${props.containsKey("description") ? props.description : "I bought a booze"}"
                    partyId: "${props.containsKey("partyId") ? props.partyId : aParty(partyRepository).id}"
                    participants: [${(props.containsKey("participants") ? props.participants : []).join(", ")}]
                }
            ) { id }
        """
    }

    def "New expense should be present in db when create expense mutation is called"() {
        given:
        authenticate()

        and:
        def yesterday = ZonedDateTime.now().minusDays(1)
        def aParty = aParty([owner: baseUser], partyRepository)

        and:
        def createExpenseMutation = createExpenseMutation(
                amount: "42,42",
                expenseDate: yesterday,
                description: "I bought a booze for everyone",
                partyId: aParty.id
        )

        when:
        String newExpenseId = postMutation(createExpenseMutation, "createExpense").id

        and:
        def actualExpense = expenseRepository.findById(newExpenseId.toLong()).get()

        then:
        actualExpense.id == newExpenseId.toLong()
        actualExpense.description == "I bought a booze for everyone"
        actualExpense.expenseDate == yesterday
        actualExpense.amount == "42,42"
        actualExpense.party.id == aParty.id
    }

    @Ignore // TODO REMOVE WHEN PAYMENTS ARE DONE
    def "There should be as many expense payments as there is expense participants"() {
        given:
        authenticate()

        and:
        def firstExpenseParticipant = aClient(userRepository)
        def secondExpenseParticipant = aClient(userRepository)
        def thirdExpenseParticipant = aClient(userRepository)

        and:
        def createExpenseMutation = createExpenseMutation(
                participants: [firstExpenseParticipant.id, secondExpenseParticipant.id, thirdExpenseParticipant.id]
        )

        when:
        String newExpenseId = postMutation(createExpenseMutation, "createExpense").id

        and:
        def actualExpense = expenseRepository.findById(newExpenseId.toLong()).get()
        def actualPayments = paymentRepository.findAllByExpenseId(newExpenseId.toLong())

        then:
        actualExpense.id == newExpenseId.toLong()
        actualPayments.size() == 3
        actualPayments.any { it.user.id == firstExpenseParticipant.id }
        actualPayments.any { it.user.id == secondExpenseParticipant.id }
        actualPayments.any { it.user.id == thirdExpenseParticipant.id }
    }

    def "Should not send a payment to a party owner"() {
        given:
        authenticate()

        and:
        def createExpenseMutation = createExpenseMutation(
                participants: [baseUser.id]
        )

        when:
        String newExpenseId = postMutation(createExpenseMutation, "createExpense").id

        and:
        def actualExpense = expenseRepository.findById(newExpenseId.toLong()).get()
        def actualPayments = paymentRepository.findAllByExpenseId(newExpenseId.toLong())

        then:
        actualExpense.id == newExpenseId.toLong()
        actualPayments.size() == 0
    }

    @Unroll
    def "Should not accept negative value for expense amount"() {
        given:
        authenticate()

        and:
        def createExpenseMutation = createExpenseMutation(
                amount: amount,
                expenseDate: expenseDate
        )

        when:
        def response = postMutation(createExpenseMutation, "createExpense", true)

        and:
        def actualExpenses = expenseRepository.findAll()

        then:
        actualExpenses.empty
        response[0].errorType == errorType
        response[0].message.contains(message)

        where:
        amount   | expenseDate                      | errorType         | message
        "42,42"  | ZonedDateTime.now().plusDays(1)  | "ValidationError" | "Expense date must be in the past"
        "-42,42" | ZonedDateTime.now().minusDays(1) | "ValidationError" | "Amount must be positive"
    }

    def "Should save payer as the logged in user"() {
        given:
        authenticate()

        and:
        def createExpenseMutation = createExpenseMutation()

        when:
        String newExpenseId = postMutation(createExpenseMutation, "createExpense").id

        and:
        def actualExpense = expenseRepository.findById(newExpenseId.toLong()).get()

        then:
        actualExpense.id == newExpenseId.toLong()
        actualExpense.user.id == baseUser.id
    }

    def "Should throw an error when party that user wants to save expense in does not exist"() {
        given:
        authenticate()

        and:
        def createExpenseMutation = createExpenseMutation(
                partyId: 242424
        )

        when:
        def response = postMutation(createExpenseMutation, "createExpense", true)

        and:
        def actualExpenses = expenseRepository.findAll()

        then:
        actualExpenses.empty
        response[0].errorType == 'ValidationError'
        response[0].message.contains("Party with such id must exist in database")
    }
//    @Unroll
//    def "Should mark expense status as resolved only when all payments are confirmed"() {
//        expect:
//        false
//    }
}
