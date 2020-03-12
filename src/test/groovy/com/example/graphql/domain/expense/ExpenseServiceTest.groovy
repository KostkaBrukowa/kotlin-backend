package com.example.graphql.domain.expense

import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.resolvers.expense.NewExpenseInput
import com.example.graphql.resolvers.expense.UpdateExpenseAmountInput
import com.example.graphql.resolvers.expense.UpdateExpenseStatusInput
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.ZonedDateTime
import java.util.stream.Collectors

import static com.example.graphql.domain.expense.ExpenseTestBuilder.defaultExpense
import static com.example.graphql.domain.party.PartyTestBuilder.defaultParty
import static com.example.graphql.domain.payment.PaymentTestBuilder.defaultPayment
import static com.example.graphql.domain.user.UserTestBuilder.defaultUser
import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames

class ExpenseServiceTest extends Specification {

    def expenseRepository = Mock(ExpenseRepository);
    def userRepository = Mock(UserRepository);
    def partyRepository = Mock(PartyRepository);
    def paymentService = Mock(PaymentService);
    def notificationService = Mock(NotificationService);
    ExpenseService expenseService = new ExpenseService(expenseRepository, paymentService, userRepository, partyRepository, notificationService)

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

    @Unroll
    def "Should not update payments when status is changed to #expenseStatusToChange"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2

        and:
        expenseRepository.findExpenseWithPayments(expenseId) >> defaultExpense([
                user         : defaultUser([id: currentUserId]),
                expenseStatus: ExpenseStatus.IN_PROGRESS_PAYING,
        ])

        when:
        expenseService.updateExpenseStatus(new UpdateExpenseStatusInput(expenseId, expenseStatusToChange), currentUserId)

        then:
        0 * paymentService.updatePaymentsAmount(_, _)

        where:
        expenseStatus                    | expenseStatusToChange
        ExpenseStatus.IN_PROGRESS_PAYING | ExpenseStatus.RESOLVED
    }

    def "Should update payments statuses when expense is declined"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2
        Long paymentId = 3

        and:
        expenseRepository.findExpenseWithPayments(expenseId) >> defaultExpense([
                user         : defaultUser([id: currentUserId]),
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING,
                payments     : [defaultPayment([id: paymentId, status: PaymentStatus.IN_PROGRESS])]
        ])

        when:
        expenseService.updateExpenseStatus(new UpdateExpenseStatusInput(expenseId, ExpenseStatus.DECLINED), currentUserId)

        then:
        1 * paymentService.updatePaymentsStatuses(List.of(paymentId), PaymentStatus.DECLINED)
    }

    def "Should delete an expense"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2
        Long paymentId = 3

        and:
        expenseRepository.findExpenseById(expenseId) >> defaultExpense([
                id           : expenseId,
                user         : defaultUser([id: currentUserId]),
                expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING,
                payments     : [defaultPayment([id: paymentId, status: PaymentStatus.IN_PROGRESS])]
        ])

        when:
        expenseService.deleteExpense(expenseId, currentUserId)

        then:
        1 * expenseRepository.removeExpense({ Expense e -> e.id == expenseId })
    }

    def "Should update expense amount"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2

        and:
        expenseRepository.findExpenseById(expenseId) >> defaultExpense([user: defaultUser([id: currentUserId]), expenseStatus: ExpenseStatus.IN_PROGRESS_REQUESTING])

        when:
        expenseService.updateExpenseAmount(new UpdateExpenseAmountInput(expenseId, 200), currentUserId)

        then:
        1 * expenseRepository.updateExpense({ Expense e -> e.amount == 200 })
    }

    @Unroll
    def "Should throw an error when expense is in wrong status (#expenseStatus) when updating amount"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2

        and:
        expenseRepository.findExpenseById(expenseId) >> defaultExpense([user: defaultUser([id: currentUserId]), expenseStatus: expenseStatus])

        when:
        expenseService.updateExpenseAmount(new UpdateExpenseAmountInput(expenseId, 200), currentUserId)

        then:
        thrown(ExpenseStatusNotValid)

        where:
        expenseStatus << [ExpenseStatus.RESOLVED, ExpenseStatus.DECLINED, ExpenseStatus.IN_PROGRESS_PAYING]
    }

    def "Should throw an error when user is not expense owner when updating amount"() {
        given:
        Long currentUserId = 1
        Long expenseId = 2

        and:
        expenseRepository.findExpenseById(expenseId) >> defaultExpense([user: defaultUser([id: 100])])

        when:
        expenseService.updateExpenseAmount(new UpdateExpenseAmountInput(expenseId, 200), currentUserId)

        then:
        thrown(UnauthorisedException)
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

    def "Should return expenses for a user"() {
        given:
        Long currentUserId = 1
        def user = defaultUser([id: currentUserId, expenses: [
                defaultExpense([id: 3]),
                defaultExpense([id: 4]),
                defaultExpense([id: 5]),
        ]])

        and:
        userRepository.findUsersWithExpenses(Set.of(currentUserId)) >> [user]

        when:
        def actualExpenses = expenseService.getExpensesForUser(currentUserId, currentUserId)

        then:
        actualExpenses.size() == 3
        actualExpenses.any { it.id == 3 }
        actualExpenses.any { it.id == 4 }
        actualExpenses.any { it.id == 5 }
    }

    def "Should throw an error when no user is found"() {
        given:
        Long currentUserId = 1

        and:
        userRepository.findUsersWithExpenses(Set.of(currentUserId)) >> []

        when:
        expenseService.getExpensesForUser(currentUserId, currentUserId)

        then:
        thrown(EntityNotFoundException)
    }

    def "Should return expenses for a party"() {
        given:
        Long currentUserId = 1
        Long partyId = 2
        def party = defaultParty([id: partyId, expenses: [
                defaultExpense([id: 3]),
                defaultExpense([id: 4]),
                defaultExpense([id: 5]),
        ]])

        and:
        partyRepository.findPartiesWithExpenses(Set.of(partyId)) >> [party]

        when:
        def actualExpenses = expenseService.getExpensesForParty(partyId, currentUserId)

        then:
        actualExpenses.size() == 3
        actualExpenses.any { it.id == 3 }
        actualExpenses.any { it.id == 4 }
        actualExpenses.any { it.id == 5 }
    }

    def "Should throw an error when party was not found"() {
        given:
        Long partyId = 1

        and:
        partyRepository.findPartiesWithExpenses(Set.of(partyId)) >> []

        when:
        expenseService.getExpensesForParty(partyId, 2)

        then:
        thrown(EntityNotFoundException)
    }

    def "Should create an expense and payments for expense with requesting status when new expense is created"() {
        given:
        Long partyId = 1
        Long currentUserId = 2
        Long otherUserId1 = 3
        Long otherUserId2 = 4
        def party = defaultParty([id: partyId, participants: [defaultUser([id: otherUserId1]), defaultUser([id: otherUserId2]), defaultUser([id: currentUserId])]])
        def newExpense = defaultExpense([:])
        def newExpenseInput = buildNewExpenseInput([
                amount      : 100.0f,
                description : 'test',
                partyId     : partyId,
                participants: [currentUserId, otherUserId1, otherUserId2]
        ])

        and:
        partyRepository.findPartiesWithParticipants(Set.of(partyId)) >> [party]

        when:
        expenseService.createExpense(newExpenseInput, currentUserId)

        then:
        1 * expenseRepository.saveNewExpense({ Expense expense -> expense.amount == 100.0f && expense.description == 'test' && expense.id == 0 }) >> newExpense
        1 * paymentService.createPaymentsForExpense(newExpense, Set.of(otherUserId1, otherUserId2))
    }

    @Unroll
    def "Should throw an error when not all expense participants are party participants"() {
        given:
        def currentUser = defaultUser([id: 100])

        and:
        def actualPartyParticipants = partyParticipants.stream().map { it -> defaultUser([id: it]) }.collect()
        actualPartyParticipants << currentUser

        and:
        def party = defaultParty([id: 101, participants: actualPartyParticipants])
        def newExpenseInput = buildNewExpenseInput([participants: expenseParticipants, partyId: party.id])

        and:
        partyRepository.findPartiesWithParticipants(Set.of(party.id)) >> [party]

        when:
        expenseService.createExpense(newExpenseInput, currentUser.id)

        then:
        thrown(ExpenseParticipantNotInPartyException)

        where:
        expenseParticipants | partyParticipants
        [3]                 | [4]
        [1, 2]              | [1, 3, 4]
        [1, 2, 3, 4]        | [1, 2, 3]
        [1]                 | []
    }

    @Unroll
    def "Should throw an error when expense creator is not in the party"() {
        given:
        def currentUser = defaultUser([id: 100])

        and:
        def actualPartyParticipants = partyParticipants.stream().map { it -> defaultUser([id: it]) }.collect()

        and:
        def party = defaultParty([id: 101, participants: actualPartyParticipants])
        def newExpenseInput = buildNewExpenseInput([participants: expenseParticipants, partyId: party.id])

        and:
        partyRepository.findPartiesWithParticipants(Set.of(party.id)) >> [party]

        when:
        expenseService.createExpense(newExpenseInput, currentUser.id)

        then:
        thrown(UnauthorisedException)

        where:
        expenseParticipants | partyParticipants
        [3]                 | [4]
        [1, 2]              | [1, 3, 4]
        [1, 2, 3, 4]        | [1, 2, 3]
        [1]                 | []
    }

    def "Should throw an error when party was not found while creating an expense"() {
        given:
        Long partyId = 1
        Long currentUserId = 2

        and:
        partyRepository.findPartiesWithParticipants(Set.of(partyId)) >> []

        when:
        expenseService.createExpense(buildNewExpenseInput(), currentUserId)

        then:
        thrown(EntityNotFoundException)
    }

    private static def buildNewExpenseInput(Map props = [:]) {
        def defaults = [
                amount      : 40.0f,
                name        : 'test expense input name',
                expenseDate : ZonedDateTime.now(),
                description : 'test description',
                partyId     : 1,
                participants: []
        ]
        verifyPropertyNames(defaults, props)
        def allProps = defaults + props

        return new NewExpenseInput(
                allProps.name as String,
                allProps.amount as Float,
                allProps.expenseDate as ZonedDateTime,
                allProps.description as String,
                allProps.partyId as Long,
                allProps.participants as List<Long>
        )
    }
}
