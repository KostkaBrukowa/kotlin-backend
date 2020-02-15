package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.PaymentStatusNotValid
import com.example.graphql.resolvers.payment.UpdatePaymentStatusInput
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import spock.lang.Specification
import spock.lang.Unroll

import static com.example.graphql.domain.expense.ExpenseTestBuilder.defaultExpense
import static com.example.graphql.domain.payment.PaymentTestBuilder.defaultPayment
import static com.example.graphql.domain.user.UserTestBuilder.defaultUser

class PaymentServiceTest extends Specification {
    PaymentRepository paymentRepository = Mock()
    PaymentService paymentService = new PaymentService(paymentRepository)

    @Unroll
    def "Should change payment status when status is changed from #statusFrom to #statusTo"() {
        given:
        Long expenseOwnerId = 1
        Long paymentOwnerId = 2
        Long paymentId = 3
        def payment = defaultPayment([
                id     : paymentId,
                user   : defaultUser([id: paymentOwnerId]),
                expense: defaultExpense([user: defaultUser([id: expenseOwnerId])]),
                status : statusFrom
        ])

        and:
        def updateStatusInput = new UpdatePaymentStatusInput(
                paymentId,
                statusTo,
        )
        def requesteeId = statusTo == PaymentStatus.CONFIRMED ? expenseOwnerId : paymentOwnerId

        and:
        paymentRepository.findPaymentWithOwnerAndExpenseOwner(paymentId) >> payment

        when:
        paymentService.updatePaymentStatus(updateStatusInput, requesteeId)

        then:
        1 * paymentRepository.updatePaymentStatus(paymentId, statusTo)

        where:
        statusFrom                | statusTo
        PaymentStatus.IN_PROGRESS | PaymentStatus.ACCEPTED
        PaymentStatus.IN_PROGRESS | PaymentStatus.DECLINED
        PaymentStatus.PAID        | PaymentStatus.CONFIRMED
        PaymentStatus.ACCEPTED    | PaymentStatus.DECLINED
        PaymentStatus.ACCEPTED    | PaymentStatus.PAID
        PaymentStatus.DECLINED    | PaymentStatus.ACCEPTED
    }

    @Unroll
    def "Should not change payment status when status is changed from #statusFrom to #statusTo"() {
        given:
        def expenseOwnerId = 1
        def paymentOwnerId = 2
        def paymentId = 3
        def payment = defaultPayment([
                id     : paymentId,
                user   : defaultUser([id: paymentOwnerId]),
                expense: defaultExpense([user: defaultUser([id: expenseOwnerId])]),
                status : statusFrom
        ])

        and:
        def updateStatusInput = new UpdatePaymentStatusInput(
                paymentId,
                statusTo,
        )
        def requesteeId = statusTo == PaymentStatus.CONFIRMED ? expenseOwnerId : paymentOwnerId

        and:
        paymentRepository.findPaymentWithOwnerAndExpenseOwner(paymentId) >> payment

        when:
        paymentService.updatePaymentStatus(updateStatusInput, requesteeId)

        then:
        def e = thrown(PaymentStatusNotValid)

        0 * paymentRepository.updatePaymentStatus(_)
        e.message.contains("Payment status was not valid, status is")

        where:
        statusFrom                | statusTo
        PaymentStatus.IN_PROGRESS | PaymentStatus.CONFIRMED
        PaymentStatus.IN_PROGRESS | PaymentStatus.PAID
        PaymentStatus.ACCEPTED    | PaymentStatus.CONFIRMED
        PaymentStatus.ACCEPTED    | PaymentStatus.IN_PROGRESS
        PaymentStatus.DECLINED    | PaymentStatus.PAID
        PaymentStatus.DECLINED    | PaymentStatus.CONFIRMED
        PaymentStatus.DECLINED    | PaymentStatus.IN_PROGRESS
        PaymentStatus.PAID        | PaymentStatus.ACCEPTED
        PaymentStatus.PAID        | PaymentStatus.IN_PROGRESS
        PaymentStatus.PAID        | PaymentStatus.DECLINED
        PaymentStatus.CONFIRMED   | PaymentStatus.IN_PROGRESS
        PaymentStatus.CONFIRMED   | PaymentStatus.PAID
        PaymentStatus.CONFIRMED   | PaymentStatus.DECLINED
        PaymentStatus.CONFIRMED   | PaymentStatus.ACCEPTED
    }

    def "Should throw an error when not an expense owner tries to change status to confirmed"() {
        given:
        def expenseOwnerId = 1
        def paymentOwnerId = 2
        def paymentId = 3
        def payment = defaultPayment([
                id     : paymentId,
                user   : defaultUser([id: paymentOwnerId]),
                expense: defaultExpense([user: defaultUser([id: expenseOwnerId])]),
                status : PaymentStatus.PAID
        ])

        and:
        def updateStatusInput = new UpdatePaymentStatusInput(
                paymentId,
                PaymentStatus.CONFIRMED,
        )

        and:
        paymentRepository.findPaymentWithOwnerAndExpenseOwner(paymentId) >> payment

        when:
        paymentService.updatePaymentStatus(updateStatusInput, 123)

        then:
        thrown UnauthorisedException

        0 * paymentRepository.updatePaymentStatus(_)
    }
}
