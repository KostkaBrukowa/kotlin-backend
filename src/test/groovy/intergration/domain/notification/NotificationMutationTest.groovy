package intergration.domain.notification

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.notification.PersistentNotificationRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPaymentRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.resolvers.message.MessageType
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import java.time.ZonedDateTime

import static intergration.domain.message.MessageMutationTest.createMessageMutation
import static intergration.utils.builders.PersistentBulkPaymentTestBuilder.aBulkPayment
import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
import static intergration.utils.builders.PersistentPersistentNotificationTestBuilder.aNotification
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class NotificationMutationTest extends BaseIntegrationSpec {
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

    @Autowired
    PersistentBulkPaymentRepository bulkPaymentRepository

    @Autowired
    PersistentNotificationRepository notificationRepository

    def "should send notifications for every payment of an expense"() {
        given:
        authenticate()

        and:
        def firstExpenseParticipant = aClient(userRepository)
        def secondExpenseParticipant = aClient(userRepository)
        def thirdExpenseParticipant = aClient(userRepository)
        def party = aParty([
                owner       : baseUser,
                participants: [firstExpenseParticipant, secondExpenseParticipant, thirdExpenseParticipant]
        ], partyRepository)

        and:
        def createExpenseMutation = createExpenseMutation(
                participants: [firstExpenseParticipant.id, secondExpenseParticipant.id, thirdExpenseParticipant.id],
                partyId: party.id
        )

        when:
        postMutation(createExpenseMutation, "createExpense")

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 3
        actualNotifications.any { it.receiver.id == firstExpenseParticipant.id && it.actor.id == baseUser.id }
        actualNotifications.any { it.receiver.id == secondExpenseParticipant.id && it.actor.id == baseUser.id }
        actualNotifications.any { it.receiver.id == thirdExpenseParticipant.id && it.actor.id == baseUser.id }
    }

    def "should create party requests notifications for all participants"() {
        given:
        authenticate()

        and:
        def createPartyMutation = { String firstUserId, String secondUserId ->
            """
            createParty(
                newPartyInput: {
                  name: "test party"
                  description: "test description"
                  participants: [
                        "${firstUserId}"
                        "${secondUserId}"
                  ]
                  startDate:  "${ZonedDateTime.now().plusDays(10)}"
                }
            ) {
                id
            }
        """
        }

        and:
        String firstUserId = aClient(userRepository).id
        String secondUserId = aClient(userRepository).id

        when:
        postMutation(createPartyMutation(firstUserId, secondUserId), "createParty")

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 2
        actualNotifications.any { it.receiver.id == firstUserId.toLong() && it.actor.id == baseUser.id }
        actualNotifications.any { it.receiver.id == secondUserId.toLong() && it.actor.id == baseUser.id }
    }

    def "should create new payment notification for status update"() {
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
        postMutation(bulkPaymentsMutation(baseUser.id.toString()))

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 4
        actualNotifications.any { it.receiver.id == baseUser.id && it.actor.id == client.id }
    }

    def "should create new message notification for each party member"() {
        given:
        authenticate()

        and:
        def client1 = aClient(userRepository)
        def client2 = aClient(userRepository)
        def client3 = aClient(userRepository)
        def party = aParty([owner: client1, participants: [baseUser, client2, client3]], partyRepository)

        when:
        postMutation(createMessageMutation(party.id, MessageType.PARTY, 'test message'))

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 3
        actualNotifications.any { it.receiver.id == client1.id }
        actualNotifications.any { it.receiver.id == client2.id }
        actualNotifications.any { it.receiver.id == client3.id }
        actualNotifications.every { it.actor.id == baseUser.id && it.objectId == party.id }
    }

    def "should create new message notification for each expense participant"() {
        given:
        authenticate()

        and: 'users'
        def client1 = aClient(userRepository)
        def client2 = aClient(userRepository)
        def client3 = aClient(userRepository)

        and: 'expense'
        def expense = anExpense([user: client1], expenseRepository)

        and: 'payments'
        aPayment([expense: expense, user: baseUser], paymentRepository)
        aPayment([expense: expense, user: client2], paymentRepository)
        aPayment([expense: expense, user: client3], paymentRepository)

        when:
        postMutation(createMessageMutation(expense.id, MessageType.EXPENSE, 'test message'))

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 3
        actualNotifications.any { it.receiver.id == client1.id }
        actualNotifications.any { it.receiver.id == client2.id }
        actualNotifications.any { it.receiver.id == client3.id }
        actualNotifications.every { it.actor.id == baseUser.id && it.objectId == expense.id }
    }

    def "should create new message notification for each payment participant"() {
        given:
        authenticate()

        and: 'users'
        def client1 = aClient(userRepository)
        def client2 = aClient(userRepository)
        def client3 = aClient(userRepository)

        and: 'expense'
        def expense = anExpense([user: client1], expenseRepository)

        and: 'payments'
        def payment = aPayment([expense: expense, user: baseUser], paymentRepository)
        aPayment([expense: expense, user: client2], paymentRepository)
        aPayment([expense: expense, user: client3], paymentRepository)

        when:
        postMutation(createMessageMutation(payment.id, MessageType.PAYMENT, 'test message'))

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 1
        actualNotifications.any { it.receiver.id == client1.id }
        actualNotifications.every { it.actor.id == baseUser.id && it.objectId == payment.id }
    }

    def "should create new message notification for each bulk payment participant"() { //
        given:
        authenticate()

        and: 'users'
        def client1 = aClient(userRepository)

        and: 'payments'
        def payment = aBulkPayment([payer: baseUser, receiver: client1], bulkPaymentRepository)

        when:
        postMutation(createMessageMutation(payment.id, MessageType.BULK_PAYMENT, 'test message'))

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 1
        actualNotifications.any { it.receiver.id == client1.id }
        actualNotifications.every { it.actor.id == baseUser.id && it.objectId == payment.id }
    }

    def "Should mark all notifications as read"() { //TODO CORRECT THIS TEST
        given:
        authenticate()

        and:
        def notifications = [
                aNotification([receiver: baseUser, actor: aClient(userRepository), isRead: true], notificationRepository),
                aNotification([receiver: baseUser, actor: aClient(userRepository), isRead: false], notificationRepository),
                aNotification([receiver: baseUser, actor: aClient(userRepository), isRead: false], notificationRepository),
        ]

        and:
        def bulkPaymentsMutation = ({ String id ->
            """
            markNotificationsAsRead(
                notificationsIds: [
                    ${notifications[0].id}
                    ${notifications[1].id}
                    ${notifications[2].id}
                ]
            )
        """
        })

        when:
        postMutation(bulkPaymentsMutation(baseUser.id.toString()))

        and:
        def actualNotifications = notificationRepository.findAll()

        then:
        actualNotifications.size() == 3
        actualNotifications.every { it.isRead }
    }

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
}
