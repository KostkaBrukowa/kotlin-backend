package intergration.domain.expense

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.expense.ExpenseStatus
import com.example.graphql.domain.payment.PaymentStatus
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
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
        String name = props.containsKey("name") ? props.name : 'Test name'
        String amount = props.containsKey("amount") ? props.amount : "42.42"
        ZonedDateTime expenseDate = props.containsKey("expenseDate") ? props.expenseDate : ZonedDateTime.now().minusDays(1)
        String description = props.containsKey("description") ? props.description : "I bought a booze"
        Long partyId = props.containsKey("partyId") ? props.partyId : aParty([owner: baseUser], partyRepository).id
        String participantsIds = (props.containsKey("participants") ? props.participants : []).join(", ")

        return """
            createExpense(
                newExpenseInput: {
                    name: "${name}"
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
                name: 'test name',
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
        actualExpense.name == 'test name'
        actualExpense.description == "I bought a booze for everyone"
        actualExpense.expenseDate == yesterday
        actualExpense.amount == 42.42f
        actualExpense.party.id == aParty.id
    }

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
        response[0].errorType == 'DataFetchingException'
        response[0].message.contains('Entity party was not found')
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

    def "Should update existing expense"() {
        given:
        authenticate()

        and:
        def twoDaysBefore = ZonedDateTime.now().minusDays(1)
        def aParty = aParty([owner: baseUser, participants: [baseUser]], partyRepository)
        def anExpense = anExpense([
                user       : baseUser,
                party      : aParty,
                expenseDate: ZonedDateTime.now().minusDays(1),
                description: "description before update",
                name       : "name before update"
        ], expenseRepository)

        and:
        def updateExpenseMutation = """
            updateExpense(
                updateExpenseInput: {
                    id: ${anExpense.id}
                    expenseDate: "${twoDaysBefore}"
                    description: "description after update"
                    name: "name after update"
                }
            ) { id }
        """

        when:
        String newExpenseId = postMutation(updateExpenseMutation, "updateExpense").id

        and:
        def actualExpense = expenseRepository.findById(anExpense.id).get()

        then:
        actualExpense.id == newExpenseId.toLong()
        actualExpense.name == "name after update"
        actualExpense.description == "description after update"
        actualExpense.expenseDate == twoDaysBefore
    }

    def "Should update expense's amount with different mutation, and should mark all payments as in progress"() {
        given:
        authenticate()

        and:
        def anExpense = anExpense([
                user         : baseUser,
                party        : aParty([owner: baseUser], partyRepository),
                amount       : 44.44,
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING
        ], expenseRepository)
        aPayment([expense: anExpense, user: aClient(userRepository), status: PaymentStatus.ACCEPTED], paymentRepository)
        aPayment([expense: anExpense, user: aClient(userRepository), status: PaymentStatus.DECLINED], paymentRepository)
        aPayment([expense: anExpense, user: aClient(userRepository), status: PaymentStatus.ACCEPTED], paymentRepository)


        and:
        def updateExpenseAmountMutation = """
            updateExpenseAmount(
                updateExpenseAmountInput: {
                    id: ${anExpense.id}
                    amount: 142.44
                }
            ) { id }
        """

        when:
        postMutation(updateExpenseAmountMutation)

        and:
        def actualExpense = expenseRepository.findById(anExpense.id).get()
        def actualPayments = paymentRepository.findAllByExpenseId(anExpense.id)

        then:
        actualExpense.amount == 142.44f
        actualPayments.size() == 3
        actualPayments.every { it.paymentStatus == PaymentStatus.IN_PROGRESS }
    }

    def "Should remove all payments when expense is deleted"() {
        given:
        authenticate()

        and:
        def anExpense = anExpense([
                user         : baseUser,
                party        : aParty([owner: baseUser], partyRepository),
                amount       : 44.44,
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING
        ], expenseRepository)
        aPayment([expense: anExpense, user: aClient(userRepository), status: PaymentStatus.ACCEPTED], paymentRepository)
        aPayment([expense: anExpense, user: aClient(userRepository), status: PaymentStatus.DECLINED], paymentRepository)
        aPayment([expense: anExpense, user: aClient(userRepository), status: PaymentStatus.ACCEPTED], paymentRepository)


        and:
        def deleteExpenseMutation = """deleteExpense(expenseId: ${anExpense.id})"""

        when:
        postMutation(deleteExpenseMutation)

        and:
        def actualExpense = expenseRepository.findById(anExpense.id)
        def actualPayments = paymentRepository.findAllByExpenseId(anExpense.id)

        then:
        actualExpense.empty
        actualPayments.empty
    }

    @Unroll
    def "Should #shouldUpdate update expense amount when expense status is #expenseStatus"() {
        given:
        authenticate()

        and:
        def anExpense = anExpense([
                user         : baseUser,
                party        : aParty([owner: aClient(userRepository)], partyRepository),
                amount       : 44.44,
                expenseStatus: expenseStatus
        ], expenseRepository)

        and:
        def updateExpenseAmountMutation = """
            updateExpenseAmount(
                updateExpenseAmountInput: {
                    id: ${anExpense.id}
                    amount: 142.44
                }
            ) { id }
        """

        when:
        def response = postMutation(updateExpenseAmountMutation, "updateExpenseAmount", !shouldUpdate)

        and:
        def actualExpense = expenseRepository.findById(anExpense.id).get()

        then:
        if (shouldUpdate) {
            assert actualExpense.amount == 142.44f
        } else {
            assert response[0].errorType == 'ValidationError'
            assert response[0].message.contains("Expense status was not valid, status is ${expenseStatus}")
        }

        where:
        expenseStatus                        | shouldUpdate
        ExpenseStatus.IN_PROGRESS_REQUESTING | true
        ExpenseStatus.IN_PROGRESS_PAYING     | false
        ExpenseStatus.DECLINED               | false
        ExpenseStatus.RESOLVED               | false
    }

    @Unroll
    def "Should delete #shouldDelete existing expense when expense status is is #expenseStatus"() {
        given:
        authenticate()

        and:
        def anExpense = anExpense([
                user         : baseUser,
                party        : aParty([owner: baseUser], partyRepository),
                amount       : 44.44,
                expenseStatus: expenseStatus

        ], expenseRepository)

        and:
        def deleteExpenseMutation = """deleteExpense(expenseId: ${anExpense.id})"""

        when:
        def response = postMutation(deleteExpenseMutation, "deleteExpense", !shouldDelete)

        and:
        def actualExpense = expenseRepository.findById(anExpense.id)

        then:
        if (shouldDelete) {
            assert actualExpense.empty
        } else {
            assert !actualExpense.empty
            assert response[0].errorType == 'ValidationError'
            assert response[0].message.contains("Expense status was not valid, status is ${expenseStatus}")
        }

        where:
        expenseStatus                        | shouldDelete
        ExpenseStatus.IN_PROGRESS_REQUESTING | true
        ExpenseStatus.IN_PROGRESS_PAYING     | false
        ExpenseStatus.DECLINED               | false
        ExpenseStatus.RESOLVED               | false
    }

    @Unroll
    def "Should change #shouldChange request status only when payments are in correct statuses #paymentStatuses"() {
        given:
        authenticate()

        and:
        def anExpense = anExpense([
                user         : baseUser,
                party        : aParty([owner: aClient(userRepository)], partyRepository),
                amount       : 44.44,
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING
        ], expenseRepository)

        paymentStatuses.forEach {
            aPayment([expense: anExpense, status: it, user: aClient(userRepository)], paymentRepository)
        }

        and:
        def updateExpenseStatusMutation = """
            changeExpenseStatus(
                updateExpenseStatusInput: {
                    id: ${anExpense.id},
                    expenseStatus: IN_PROGRESS_PAYING
                }
            ) { id }
        """

        when:
        def response = postMutation(updateExpenseStatusMutation, null, !shouldChange)

        and:
        def actualExpense = expenseRepository.findById(anExpense.id).get()

        then:
        if (shouldChange) {
            assert actualExpense.expenseStatus == ExpenseStatus.IN_PROGRESS_PAYING
        } else {
            assert actualExpense.expenseStatus == ExpenseStatus.IN_PROGRESS_REQUESTING
            assert response[0].errorType == 'ValidationError'
            assert response[0].message.contains('Payment status was not valid, status is')
        }
        where:
        paymentStatuses                                                           | shouldChange
        [PaymentStatus.PAID, PaymentStatus.PAID, PaymentStatus.DECLINED]          | false
        [PaymentStatus.ACCEPTED, PaymentStatus.ACCEPTED, PaymentStatus.DECLINED]  | true
        [PaymentStatus.CONFIRMED, PaymentStatus.ACCEPTED, PaymentStatus.DECLINED] | false
        [PaymentStatus.DECLINED, PaymentStatus.DECLINED, PaymentStatus.DECLINED]  | true
    }

    def "Should calculate amounts for payments after change to confirmed status"() {
        given:
        authenticate()

        and:
        def anExpense = anExpense([
                user         : baseUser,
                party        : aParty([owner: aClient(userRepository)], partyRepository),
                amount       : 100.0,
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING
        ], expenseRepository)

        aPayment([expense: anExpense, status: PaymentStatus.ACCEPTED, user: aClient(userRepository), amount: null], paymentRepository)
        aPayment([expense: anExpense, status: PaymentStatus.ACCEPTED, user: aClient(userRepository), amount: null], paymentRepository)
        aPayment([expense: anExpense, status: PaymentStatus.ACCEPTED, user: aClient(userRepository), amount: null], paymentRepository)

        and:
        def changeExpenseStatusMutation = """
            changeExpenseStatus(
                updateExpenseStatusInput: {
                    id: ${anExpense.id},
                    expenseStatus: IN_PROGRESS_PAYING
                }
            ) { id }
        """

        when:
        postMutation(changeExpenseStatusMutation)

        and:
        def actualExpense = expenseRepository.findById(anExpense.id).get()
        def actualPayments = paymentRepository.findAllByExpenseId(anExpense.id)

        then:
        actualExpense.expenseStatus == ExpenseStatus.IN_PROGRESS_PAYING
        actualPayments.every { it.amount == 25.0 }
    }

    def "Should reset expense payments when expense is declined"() {
        given:
        authenticate()

        and:
        def anExpense = anExpense([
                user         : baseUser,
                party        : aParty([owner: aClient(userRepository)], partyRepository),
                amount       : 100.0,
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING
        ], expenseRepository)

        aPayment([expense: anExpense, status: PaymentStatus.ACCEPTED, user: aClient(userRepository), amount: null], paymentRepository)
        aPayment([expense: anExpense, status: PaymentStatus.ACCEPTED, user: aClient(userRepository), amount: null], paymentRepository)
        aPayment([expense: anExpense, status: PaymentStatus.ACCEPTED, user: aClient(userRepository), amount: null], paymentRepository)

        and:
        def changeExpenseStatusMutation = """
            changeExpenseStatus(
                updateExpenseStatusInput: {
                    id: ${anExpense.id},
                    expenseStatus: DECLINED
                }
            ) { id }
        """

        when:
        postMutation(changeExpenseStatusMutation)

        and:
        def actualExpense = expenseRepository.findById(anExpense.id).get()
        def actualPayments = paymentRepository.findAllByExpenseId(anExpense.id)

        then:
        actualExpense.expenseStatus == ExpenseStatus.DECLINED
        actualPayments.every { it.paymentStatus == PaymentStatus.DECLINED }
    }
}
