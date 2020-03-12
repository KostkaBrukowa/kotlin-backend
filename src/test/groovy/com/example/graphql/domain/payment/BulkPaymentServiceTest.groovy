package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.PaymentStatusNotValid
import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.resolvers.payment.UpdateBulkPaymentStatusInput
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

import static com.example.graphql.domain.expense.ExpenseTestBuilder.defaultExpense
import static com.example.graphql.domain.payment.BulkBulkPaymentTestBuilder.defaultBulkPayment
import static com.example.graphql.domain.payment.PaymentTestBuilder.defaultPayment
import static com.example.graphql.domain.user.UserTestBuilder.defaultUser

class BulkPaymentServiceTest extends Specification {
    PaymentRepository paymentRepository = Mock()
    BulkPaymentRepository bulkPaymentRepository = Mock()
    NotificationService notificationService = Mock()
    BulkPaymentService bulkPaymentService = new BulkPaymentService(bulkPaymentRepository, paymentRepository, notificationService)

    def "should return bulk payments"() {
        given:
        Long currentUserId = 1
        Long bulkPaymentId1 = 2
        Long bulkPaymentId2 = 3

        when:
        def response = bulkPaymentService.findUserBulkPayments(currentUserId)

        then:
        response.size() == 2
        response.any { it -> it.id == bulkPaymentId1 }
        response.any { it -> it.id == bulkPaymentId2 }
        1 * bulkPaymentRepository.findBulkPaymentsByPayerIdOrReceiver(currentUserId) >> [
                defaultBulkPayment([id: bulkPaymentId1]),
                defaultBulkPayment([id: bulkPaymentId2])
        ]
    }

    @Unroll
    def "should calculate correct bulk payment amount #owedAmount"() {
        given:
        Long firstUserId = 1
        Long secondUserId = 2
        Long currentUserId = 4

        and:
        def firstUserPayments = buildPayments(firstUserId, secondUserId, firstUserPaymentsAmounts)
        def secondUserPayments = buildPayments(secondUserId, firstUserId, secondUserPaymentsAmounts)

        and:
        paymentRepository.findPaymentsWithOwnerAndExpenseOwner(_) >> (firstUserPayments + secondUserPayments).toList()

        when:
        bulkPaymentService.convertPaymentsToBulkPayment(paymentsToIds(firstUserPayments, secondUserPayments), currentUserId)

        then:
        1 * bulkPaymentRepository.createBulkPayment({ Float it -> compareFloats(it, owedAmount) }, payerId, receiverId) >> defaultBulkPayment([:])

        where:
        firstUserPaymentsAmounts | secondUserPaymentsAmounts | owedAmount | payerId | receiverId
        [12]                     | [22]                      | 10         | 2       | 1
        [12, 22, 33.4]           | []                        | 67.4f      | 1       | 2
        []                       | [12, 22, 33.4]            | 67.4f      | 2       | 1
        [22]                     | [22]                      | 0          | 2       | 1
    }

    @Unroll
    def "should throw an error when payments to convert are in wrong status #status"() {
        given:
        Long firstUserId = 1
        Long secondUserId = 2
        Long currentUserId = 4

        and:
        def payments = buildPayments(firstUserId, secondUserId, [10, 10], PaymentStatus.DECLINED)

        and:
        paymentRepository.findPaymentsWithOwnerAndExpenseOwner(_) >> payments.toList()
        bulkPaymentRepository.createBulkPayment(_, _, _) >> defaultBulkPayment([:])

        when:
        bulkPaymentService.convertPaymentsToBulkPayment(paymentsToIds(payments), currentUserId)

        then:
        thrown(PaymentStatusNotValid)

        where:
        status << [PaymentStatus.DECLINED, PaymentStatus.PAID, PaymentStatus.CONFIRMED, PaymentStatus.BULKED]
    }

    def "should update bulk payment status when bulk payments is in correct status"() {
        given:
        Long bulkPaymentId = 3
        Long currentUserId = 4

        and:
        def bulkPaymentUpdateInput = new UpdateBulkPaymentStatusInput(bulkPaymentId, statusTo)

        and:
        bulkPaymentRepository.findBulkPaymentById(bulkPaymentId) >> defaultBulkPayment([
                id      : bulkPaymentId,
                status  : statusFrom,
                payer   : defaultUser([id: currentUserId]),
                receiver: defaultUser([id: currentUserId]),
        ])

        and:
        bulkPaymentRepository.createBulkPayment(_, _, _) >> defaultBulkPayment([:])

        when:
        bulkPaymentService.updatePaymentStatus(bulkPaymentUpdateInput, currentUserId)

        then:
        1 * bulkPaymentRepository.updateBulkPaymentStatus(bulkPaymentId, statusTo)

        where:
        statusFrom                    | statusTo
        BulkPaymentStatus.IN_PROGRESS | BulkPaymentStatus.PAID
        BulkPaymentStatus.PAID        | BulkPaymentStatus.IN_PROGRESS
    }

    def "should not update bulk payment status when bulk payments is in incorrect status"() {
        given:
        Long bulkPaymentId = 3
        Long currentUserId = 4

        and:
        def bulkPaymentUpdateInput = new UpdateBulkPaymentStatusInput(bulkPaymentId, statusTo)

        and:
        bulkPaymentRepository.findBulkPaymentById(bulkPaymentId) >> defaultBulkPayment([
                id      : bulkPaymentId,
                status  : statusFrom,
                payer   : defaultUser([id: currentUserId]),
                receiver: defaultUser([id: currentUserId]),
        ])

        and:
        bulkPaymentRepository.createBulkPayment(_, _, _) >> defaultBulkPayment([:])

        when:
        bulkPaymentService.updatePaymentStatus(bulkPaymentUpdateInput, currentUserId)

        then:
        thrown(BulkPaymentStatusNotValid)

        where:
        statusFrom                    | statusTo
        BulkPaymentStatus.PAID        | BulkPaymentStatus.PAID
        BulkPaymentStatus.IN_PROGRESS | BulkPaymentStatus.IN_PROGRESS
        BulkPaymentStatus.CONFIRMED   | BulkPaymentStatus.CONFIRMED
        BulkPaymentStatus.IN_PROGRESS | BulkPaymentStatus.CONFIRMED
        BulkPaymentStatus.CONFIRMED   | BulkPaymentStatus.IN_PROGRESS
        BulkPaymentStatus.CONFIRMED   | BulkPaymentStatus.PAID
    }

    def "should not let other user confirm bulk payment status"() {
        given:
        Long bulkPaymentId = 3
        Long currentUserId = 4
        Long otherUserId = 5

        and:
        def bulkPaymentUpdateInput = new UpdateBulkPaymentStatusInput(bulkPaymentId, BulkPaymentStatus.CONFIRMED)

        and:
        bulkPaymentRepository.findBulkPaymentById(bulkPaymentId) >> defaultBulkPayment([
                id      : bulkPaymentId,
                status  : BulkPaymentStatus.PAID,
                payer   : defaultUser([id: otherUserId]),
                receiver: defaultUser([id: currentUserId]),
        ])

        and:
        bulkPaymentRepository.createBulkPayment(_, _, _) >> defaultBulkPayment([:])

        when:
        bulkPaymentService.updatePaymentStatus(bulkPaymentUpdateInput, otherUserId)

        then:
        thrown(UnauthorisedException)
    }

    def "should not let other user change the status"() {
        given:
        Long bulkPaymentId = 3
        Long currentUserId = 4
        Long otherUserId = 5

        and:
        def bulkPaymentUpdateInput = new UpdateBulkPaymentStatusInput(bulkPaymentId, BulkPaymentStatus.PAID)

        and:
        bulkPaymentRepository.findBulkPaymentById(bulkPaymentId) >> defaultBulkPayment([
                id      : bulkPaymentId,
                status  : BulkPaymentStatus.IN_PROGRESS,
                payer   : defaultUser([id: currentUserId]),
                receiver: defaultUser([id: otherUserId]),
        ])

        and:
        bulkPaymentRepository.createBulkPayment(_, _, _) >> defaultBulkPayment([:])

        when:
        bulkPaymentService.updatePaymentStatus(bulkPaymentUpdateInput, otherUserId)

        then:
        thrown(UnauthorisedException)
    }

    static private List<Payment> buildPayments(Long payerId, Long receiverId, List<Float> amounts, PaymentStatus status = PaymentStatus.IN_PROGRESS) {
        return amounts.stream().map { it ->
            defaultPayment([
                    user   : defaultUser([id: payerId]),
                    expense: defaultExpense([user: defaultUser([id: receiverId])]),
                    amount : it,
                    status : status
            ])
        }.collect(Collectors.toList())
    }

    static private List<Long> paymentsToIds(List<Payment> payments1 = [], List<Payment> payments2 = []) {
        return (payments1 + payments2).stream().map { it ->
            2
        }.collect(Collectors.toList())
    }

    static private Boolean compareFloats(Float f1, Float f2) {
        return (f1 - f2).abs() < 0.00001
    }
}
