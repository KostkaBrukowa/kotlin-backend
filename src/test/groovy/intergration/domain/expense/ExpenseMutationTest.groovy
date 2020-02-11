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

    def createExpenseMutation(Map props = [:]) {
        String amount = props.containsKey("amount") ? props.amount : "42.42"
        ZonedDateTime expenseDate = props.containsKey("expenseDate") ? props.expenseDate : ZonedDateTime.now().minusDays(1)
        String description = props.containsKey("description") ? props.description : "I bought a booze"
        Long partyId = props.containsKey("partyId") ? props.partyId : aParty([owner: baseUser], partyRepository).id
        String participantsIds = (props.containsKey("participants") ? props.participants : []).join(", ")

        return """
            createExpense(
                newExpenseInput: {
                    amount: ${amount}
                    expenseDate: "${expenseDate}"
                    description: "${description}"
                    partyId: "${partyId}"
                    participants: [${participantsIds}]
                }
            ) { id }
        """
    }

    def "New expense should be present in db when create expense mutation is called"() {
        given:
        authenticate()

        and:
        def yesterday = ZonedDateTime.now().minusDays(1)
        def aParty = aParty([owner: baseUser, participants: [baseUser]], partyRepository)

        and:
        def createExpenseMutation = createExpenseMutation(
                amount: "42.42",
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
        actualExpense.amount == 42.42f
        actualExpense.party.id == aParty.id
    }

    @Ignore
    // TODO REMOVE WHEN PAYMENTS ARE DONE
    def "There should be as many expense payments as there is expense participants"() {
        given:
        authenticate()

        and:
        def firstExpenseParticipant = aClient(userRepository)
        def secondExpenseParticipant = aClient(userRepository)
        def thirdExpenseParticipant = aClient(userRepository)
        def aParty = aParty([
                owner       : baseUser,
                participants: [firstExpenseParticipant, secondExpenseParticipant, thirdExpenseParticipant]
        ], partyRepository)

        and:
        def createExpenseMutation = createExpenseMutation(
                participants: [firstExpenseParticipant.id, secondExpenseParticipant.id, thirdExpenseParticipant.id],
                partyId: aParty.id
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
        def aParty = aParty([owner: baseUser], partyRepository)
        def createExpenseMutation = createExpenseMutation(
                participants: [baseUser.id],
                partyId: aParty.id
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
    def "Should not accept invalid values #message"() {
        given:
        authenticate()

        and:
        def createExpenseMutation = createExpenseMutation(
                amount: amount,
                expenseDate: expenseDate,
                description: description
        )

        when:
        def response = postMutation(createExpenseMutation, "createExpense", true)

        and:
        def actualExpenses = expenseRepository.findAll()

        then:
        response[0].errorType == errorType
        response[0].message.contains(message)
        actualExpenses.empty

        where:
        amount   | description       | expenseDate                      | errorType         | message
        "42.42"  | "more than three" | ZonedDateTime.now().plusDays(1)  | "ValidationError" | "must be a date in the past or in the present"
        "-42.42" | "more than three" | ZonedDateTime.now().minusDays(1) | "ValidationError" | "must be greater than 0"
        "42.42"  | "ab"              | ZonedDateTime.now().minusDays(1) | "ValidationError" | "length must be between 3 and 256"
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

    def "Should throw an error when party that user wants to save expense to, does not exist"() {
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
        response[0].message.contains('Party with such id was not found')
    }

    def "Should not allow to add a user as a request participant if the user is not a party participant"() {
        given:
        authenticate()

        and:
        def expenseParticipant = aClient(userRepository)
        def aParty = aParty([
                owner       : baseUser,
                participants: [baseUser]
        ], partyRepository)

        and:
        def createExpenseMutation = createExpenseMutation(
                participants: [expenseParticipant.id],
                partyId: aParty.id
        )

        when:
        def response = postMutation(createExpenseMutation, "createExpense", true)

        and:
        def actualExpenses = expenseRepository.findAll()
        def actualPayments = paymentRepository.findAll()

        then:
        response[0].errorType == 'ValidationError'
        response[0].message.contains('Not all users were party participants')
        actualExpenses.empty
        actualPayments.empty
    }

    def "Should not allow user, who's not party participant to add a expense to a party"() {
        given:
        authenticate()

        and:
        def anOwner = aClient(userRepository)
        def aParty = aParty([
                owner       : anOwner,
                participants: [anOwner]
        ], partyRepository)

        and:
        def createExpenseMutation = createExpenseMutation(
                partyId: aParty.id
        )

        when:
        def response = postMutation(createExpenseMutation, "createExpense", true)

        and:
        def actualExpenses = expenseRepository.findAll()

        then:
        response[0].errorType == 'DataFetchingException'
        response[0].message.contains('User is not authorised to perform this action')
        actualExpenses.empty
    }
//    @Unroll
//    def "Should mark expense status as resolved only when all payments are confirmed"() {
//        expect:
//        false
//    }
}
