package intergration.domain.notification

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.notification.NotificationObjectType
import com.example.graphql.adapters.pgsql.notification.PersistentNotificationRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import java.time.ZoneId
import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
import static intergration.utils.builders.PersistentPersistentNotificationTestBuilder.aNotification
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class NotificationQueryTest extends BaseIntegrationSpec {

    @Autowired
    PersistentNotificationRepository notificationRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentExpenseRepository expenseRepository

    @Autowired
    PersistentPaymentRepository paymentRepository

    def "should return all users messages ordered by date"() {
        given:
        authenticate()

        and:
        def notification1 = notificationRepository.save(buildNotification('2020-01-27T12:33:39.536632+01:00[Europe/Warsaw]'))
        def notification2 = notificationRepository.save(buildNotification('2020-03-27T12:33:39.536632+01:00[Europe/Warsaw]'))
        def notification3 = notificationRepository.save(buildNotification('2020-02-27T12:33:39.536632+01:00[Europe/Warsaw]'))

        and:
        def getAllUserNotificationQuery = ("""findUserNotifications(userId: ${baseUser.id}) { id }""")

        when:
        def response = postQuery(getAllUserNotificationQuery)

        then:
        response.size() == 3
        response.any { it.id.toLong() == notification1.id }
        response.any { it.id.toLong() == notification2.id }
        response.any { it.id.toLong() == notification3.id }
    }

    def "should return party request notification"() {
        given:
        authenticate()

        and:
        def party = aParty([owner: aClient(userRepository)], partyRepository)

        and:
        def notification1 = notificationRepository.save(buildNotification(null, NotificationObjectType.PARTY_REQUEST, party.id))
        def notification2 = notificationRepository.save(buildNotification(null, NotificationObjectType.PARTY_REQUEST, party.id))

        and:
        def getPartyRequestNotificationQuery = (
                """findUserNotifications(userId: ${baseUser.id}) { 
                    id
                    ... on PartyRequestNotification {
                        partyId
                    }
            }
            """
        )

        when:
        def response = postQuery(getPartyRequestNotificationQuery)

        then:
        response.size() == 2
        response.any { it.id.toLong() == notification1.id }
        response.any { it.id.toLong() == notification2.id }
        response.every { it.partyId.toLong() == party.id }
    }

    def "should return expense notification"() {
        given:
        authenticate()

        and:
        def expense = anExpense([user: aClient(userRepository)], expenseRepository)

        and:
        def notification1 = notificationRepository.save(buildNotification(null, NotificationObjectType.EXPENSE, expense.id))
        def notification2 = notificationRepository.save(buildNotification(null, NotificationObjectType.EXPENSE, expense.id))

        and:
        def getExpenseNotificationQuery = (
                """findUserNotifications(userId: ${baseUser.id}) { 
                    id
                    ... on ExpenseNotification {
                        expenseId
                    }
            }
            """
        )

        when:
        def response = postQuery(getExpenseNotificationQuery)

        then:
        response.size() == 2
        response.any { it.id.toLong() == notification1.id }
        response.any { it.id.toLong() == notification2.id }
        response.every { it.expenseId.toLong() == expense.id }
    }

    def "should return payment notification"() {
        given:
        authenticate()

        and:
        def expense = anExpense([user: aClient(userRepository)], expenseRepository)
        def payment = aPayment([user: aClient(userRepository), expense: expense], paymentRepository)

        and:
        def notification1 = notificationRepository.save(buildNotification(null, NotificationObjectType.PAYMENT, payment.id))
        def notification2 = notificationRepository.save(buildNotification(null, NotificationObjectType.PAYMENT, payment.id))

        and:
        def getPaymentNotificationQuery = (
                """findUserNotifications(userId: ${baseUser.id}) { 
                    id
                    ... on PaymentNotification {
                        paymentId
                    }
            }
            """
        )

        when:
        def response = postQuery(getPaymentNotificationQuery)

        then:
        response.size() == 2
        response.any { it.id.toLong() == notification1.id }
        response.any { it.id.toLong() == notification2.id }
        response.every { it.paymentId.toLong() == payment.id }
    }

    def buildNotification(String date = null, NotificationObjectType type = NotificationObjectType.PARTY_REQUEST, Long objectId = 0) {
        return aNotification(
                [
                        receiver  : baseUser,
                        actor     : aClient(userRepository),
                        createdAt : date ? date : '2020-02-27T12:33:39.536632+01:00[Europe/Warsaw]',
                        objectType: type,
                        objectId  : objectId
                ], notificationRepository
        );
    }

    def buildDate(Integer month) {
        return ZonedDateTime.of(2020, month, 10, 10, 10, 10, 10, ZoneId.systemDefault());
    }
}
