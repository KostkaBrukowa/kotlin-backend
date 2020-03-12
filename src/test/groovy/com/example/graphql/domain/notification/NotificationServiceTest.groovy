package com.example.graphql.domain.notification

import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import spock.lang.Specification

import static com.example.graphql.domain.expense.ExpenseTestBuilder.defaultExpense
import static com.example.graphql.domain.notification.NotificationTestBuilder.defaultNotification
import static com.example.graphql.domain.partyrequest.PartyRequestTestBuilder.defaultPartyRequest
import static com.example.graphql.domain.payment.PaymentTestBuilder.defaultPayment
import static com.example.graphql.domain.user.UserTestBuilder.defaultUser

class NotificationServiceTest extends Specification {

    NotificationRepository notificationRepository = Mock()
    def notificationService = new NotificationService(notificationRepository)

    def "should return user notifications"() {
        given:
        Long currentUserId = 42

        and:
        notificationRepository.findUserNotifications(currentUserId) >> [defaultNotification([id: 1]), defaultNotification([id: 2])]

        when:
        def response = notificationService.findUserNotifications(currentUserId, currentUserId)

        then:
        response.size() == 2
        response.any { it.id == 1 }
        response.any { it.id == 2 }
    }

    def "should mark all notifications as read"() {
        given:
        Long currentUserId = 42

        and:
        notificationRepository.findNotificationsWithUsers([1, 2, 3]) >> [
                defaultNotification([id: 1, receiver: defaultUser([id: currentUserId])]),
                defaultNotification([id: 2, receiver: defaultUser([id: currentUserId])]),
                defaultNotification([id: 3, receiver: defaultUser([id: currentUserId])]),
        ]

        when:
        def response = notificationService.markNotificationsAsRead([1, 2, 3], currentUserId)

        then:
        response
        1 * notificationRepository.markNotificationsAsRead([1, 2, 3])
    }

    def "should throw an error when not all notifications belong to user"() {
        given:
        Long currentUserId = 42
        Long notCurrentUserId = 142

        and:
        notificationRepository.findNotificationsWithUsers([1, 2, 3]) >> [
                defaultNotification([id: 1, receiver: defaultUser([id: currentUserId])]),
                defaultNotification([id: 2, receiver: defaultUser([id: currentUserId])]),
                defaultNotification([id: 3, receiver: defaultUser([id: notCurrentUserId])]),
        ]

        when:
        notificationService.markNotificationsAsRead([1, 2, 3], currentUserId)

        then:
        thrown(UnauthorisedException)
    }

    def "should create expense payments notifications"() {
        given:
        Long currentUserId = 42
        Long expenseId = 43
        Long notCurrentUserId1 = 142
        Long notCurrentUserId2 = 143

        and:
        def expense = defaultExpense([
                id  : expenseId,
                user: defaultUser([id: currentUserId]),
                name: 'expense name'
        ])
        def payments = [
                defaultPayment([id: 1, user: defaultUser([id: notCurrentUserId1])]),
                defaultPayment([id: 3, user: defaultUser([id: notCurrentUserId2])]),
        ]

        when:
        notificationService.newExpensePaymentsNotification(expense, payments)

        then:
        1 * notificationRepository.sendExpenseNotifications({ List<NewExpenseNotification> notifications ->
            (
                    notifications.size() == 2
                            && notifications.any { it ->
                        (it.actorId == currentUserId && it.receiverId == notCurrentUserId1 && it.expenseId == expenseId && it.objectName == 'expense name')
                    } && notifications.any { it ->
                        (it.actorId == currentUserId && it.receiverId == notCurrentUserId2 && it.expenseId == expenseId && it.objectName == 'expense name')
                    }
            )
        })
    }

    def "should create party requests notifications"() {
        given:
        Long notCurrentUserId1 = 142
        Long notCurrentUserId2 = 143

        and:
        def partyRequests = [
                defaultPartyRequest([id: 1, user: defaultUser([id: notCurrentUserId1])]),
                defaultPartyRequest([id: 3, user: defaultUser([id: notCurrentUserId2])]),
        ]

        when:
        notificationService.newPartyRequestsNotifications(partyRequests, 44, 'some party name')

        then:
        1 * notificationRepository.sendPartyRequestsNotifications({ List<NewPartyRequestNotification> notifications ->
            (
                    notifications.size() == 2
                            && notifications.any { it ->
                        (it.actorId == 44 && it.receiverId == notCurrentUserId1 && it.partyRequestId == 1 && it.objectName == 'some party name')
                    } && notifications.any { it ->
                        (it.actorId == 44 && it.receiverId == notCurrentUserId2 && it.partyRequestId == 3 && it.objectName == 'some party name')
                    }
            )
        })
    }

    def "should create payment statuses notification"() {
        given:
        Long currentUserId = 42
        Long notCurrentUserId1 = 142
        Long notCurrentUserId2 = 143

        and:
        def expense = defaultExpense([user: defaultUser([id: currentUserId]), name: 'notification expense name'])
        def payments = [
                defaultPayment([
                        id     : 1,
                        user   : defaultUser([id: notCurrentUserId1]),
                        expense: expense
                ]),
                defaultPayment([
                        id     : 3,
                        user   : defaultUser([id: notCurrentUserId2]),
                        expense: expense
                ]),
        ]

        when:
        notificationService.updatePaymentsStatusesNotifications(payments, PaymentStatus.ACCEPTED)

        then:
        1 * notificationRepository.sendPaymentsNotifications({ List<UpdatePaymentStatusNotification> notifications ->
            (
                    notifications.size() == 2
                            && notifications.any { it ->
                        (it.actorId == notCurrentUserId1 && it.receiverId == currentUserId && it.paymentId == 1 && it.objectName == 'notification expense name')
                    } && notifications.any { it ->
                        (it.actorId == notCurrentUserId2 && it.receiverId == currentUserId && it.paymentId == 3 && it.objectName == 'notification expense name')
                    }
            )
        })
    }

}
