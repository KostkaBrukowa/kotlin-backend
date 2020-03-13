package intergration.domain.notification

import com.example.graphql.adapters.pgsql.notification.PersistentNotificationRepository
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import java.time.ZoneId
import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentPersistentNotificationTestBuilder.aNotification
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class NotificationQueryTest extends BaseIntegrationSpec {

    @Autowired
    PersistentNotificationRepository notificationRepository

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

    def buildNotification(String date) {
        return aNotification(
                [
                        receiver : baseUser,
                        actor    : aClient(userRepository),
                        createdAt: date
                ], notificationRepository
        );
    }

    def buildDate(Integer month) {
        return ZonedDateTime.of(2020, month, 10, 10, 10, 10, 10, ZoneId.systemDefault());
    }
}
