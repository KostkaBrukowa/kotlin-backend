package com.example.graphql.domain.expense

import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.party.PartyService
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.resolvers.expense.UpdateExpenseStatusInput
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

import static com.example.graphql.domain.expense.ExpenseTestBuilder.defaultExpense
import static com.example.graphql.domain.payment.PaymentTestBuilder.defaultPayment
import static com.example.graphql.domain.user.UserTestBuilder.defaultUser

class ExpenseServiceTest extends Specification {

    def expenseRepository = Mock(ExpenseRepository);
    def userRepository = Mock(UserRepository);
    def partyRepository = Mock(PartyRepository);
    def partyService = Mock(PartyService);
    def paymentService = Mock(PaymentService);
    ExpenseService expenseService = new ExpenseService(expenseRepository, partyService, paymentService, userRepository, partyRepository)

    @Unroll
    def "Should calculate correct amount for payments when expense is changed to paying"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2

        and:
        expenseRepository.findExpenseWithPayments(expenseId) >> defaultExpense([
                user         : defaultUser([id: currentUserId]),
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING,
                amount       : expenseAmount,
                payments     : paymentsStatuses.stream().map {
                    defaultPayment([status: it])
                }.collect(Collectors.toList()) as List<Payment>
        ])

        when:
        expenseService.updateExpenseStatus(new UpdateExpenseStatusInput(expenseId, ExpenseStatus.IN_PROGRESS_PAYING), currentUserId)

        then:
        1 * paymentService.updatePaymentsAmount(_, expectedAmount)

        where:
        expenseAmount | paymentsStatuses                                                                                 | expectedAmount
        44            | [PaymentStatus.ACCEPTED, PaymentStatus.ACCEPTED]                                                 | 14.666667f
        44            | [PaymentStatus.DECLINED, PaymentStatus.DECLINED]                                                 | 44
        44            | [PaymentStatus.ACCEPTED, PaymentStatus.DECLINED]                                                 | 22
        44            | [PaymentStatus.ACCEPTED, PaymentStatus.ACCEPTED, PaymentStatus.DECLINED, PaymentStatus.ACCEPTED] | 11
    }

    def "Should return expense by id"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2
        def expense = defaultExpense([id: expenseId])

        and:
        expenseRepository.findExpenseById(expenseId) >> expense

        when:
        def actualExpense = expenseService.findExpenseById(expenseId, currentUserId)

        then:
        actualExpense == expense
    }
}
