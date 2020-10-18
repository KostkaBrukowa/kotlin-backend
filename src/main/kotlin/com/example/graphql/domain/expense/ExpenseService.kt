package com.example.graphql.domain.expense

import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyKind
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.resolvers.expense.NewExpenseInput
import com.example.graphql.resolvers.expense.UpdateExpenseInput
import com.example.graphql.resolvers.expense.UpdateExpenseStatusInput
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.InvalidActionException
import com.example.graphql.schema.exceptions.handlers.SimpleValidationException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class ExpenseService(
        private val expenseRepository: ExpenseRepository,
        private val paymentService: PaymentService,
        private val userRepository: UserRepository,
        private val partyRepository: PartyRepository
) {
    // GET
    fun findExpenseById(expenseId: Long, currentUserId: Long): Expense? {
        return expenseRepository.findExpenseById(expenseId)
    }

    fun getExpensesForUser(userId: Long, currentUserId: Long): List<Expense> {
        if (userId != currentUserId) throw UnauthorisedException()

        return userRepository.findUsersWithExpenses(setOf(userId)).firstOrNull()?.expenses
                ?: throw EntityNotFoundException("user")
    }

    fun getExpensesForParty(partyId: Long, currentUserId: Long): List<Expense> {
        return partyRepository.findPartiesWithExpenses(setOf(partyId)).firstOrNull()?.expenses
                ?: throw EntityNotFoundException("party")
    }

    // CREATE
    fun createExpense(expenseInput: NewExpenseInput, currentUserId: Long): Expense {
        if (expenseInput.partyType != PartyKind.FRIENDS && expenseInput.partyId == null) {
            throw InvalidActionException("Cannot add expense without party Id")
        }

        val expenseParty = if (expenseInput.partyId != null) partyRepository.findPartiesWithParticipants(setOf(expenseInput.partyId.toLong())).firstOrNull()
                ?: throw EntityNotFoundException("party") else findOrCreateFriendsParty(expenseInput, currentUserId)

        val expenseInputWithPartyId = expenseInput.copy(partyId = expenseParty.id.toString())
        val expenseParticipants = expenseInputWithPartyId.participants.filter { it.toLong() != currentUserId }.map { it.toLong() }.toSet()

        requirePartyParticipantsIncludeExpenseParticipants(expenseParticipants.map { it }.toSet(), expenseParty, currentUserId)

        val newExpense = expenseRepository.saveNewExpense(expenseInputWithPartyId.toDomain(currentUserId))

        paymentService.createPaymentsForExpense(newExpense, expenseParticipants)

        return newExpense
    }

    private fun findOrCreateFriendsParty(expenseInput: NewExpenseInput, userId: Long): Party {
        val usersParties = partyRepository.getAllUsersPartiesWithParticipants(userId)
        val expenseInputParticipants = (expenseInput.participants + userId.toString()).map { it.toLong() }.distinct()

        val existingFriendsParty = usersParties.filter { it.type === PartyKind.FRIENDS }.find {
            val partyParticipantsIds = it.participants.map { participant -> participant.id }

            expenseInputParticipants.containsAll(partyParticipantsIds) && partyParticipantsIds.containsAll(expenseInputParticipants)
        }

        return if (existingFriendsParty == null) {
            val currentUser = User(id = userId)

            partyRepository.saveNewParty(Party(
                    owner = currentUser, participants = expenseInputParticipants.map { User(id = it) }, type = PartyKind.FRIENDS
            )).copy(participants = expenseInputParticipants.map { User(id = it) })
        } else existingFriendsParty
    }

    // UPDATE
    fun updateExpense(updateExpenseInput: UpdateExpenseInput, currentUserId: Long): Expense {
        val expenseToUpdate = expenseRepository.findExpenseById(updateExpenseInput.id.toLong())
                ?: throw EntityNotFoundException("expense")

        requireExpenseOwner(expenseToUpdate, currentUserId)

        val updatedExpense = expenseToUpdate.copy(
                name = updateExpenseInput.name,
                description = updateExpenseInput.description,
                expenseDate = updateExpenseInput.expenseDate,
                amount = updateExpenseInput.amount
        )

        updateExpenseAmount(expenseToUpdate, updateExpenseInput.amount)
        expenseRepository.updateExpense(updatedExpense)

        return updatedExpense
    }

    private fun updateExpenseAmount(expenseToUpdate: Expense, amount: Float) {
        if (expenseToUpdate.amount == amount)
            return

        requireExpenseStatuses(expenseToUpdate, listOf(ExpenseStatus.IN_PROGRESS_REQUESTING))

        paymentService.resetPaymentsStatuses(expenseToUpdate.id)
    }

    fun updateExpenseStatus(updateExpenseStatusInput: UpdateExpenseStatusInput, currentUserId: Long): Expense {
        val expenseToUpdate = expenseRepository.findExpenseWithPayments(updateExpenseStatusInput.id.toLong())
                ?: throw EntityNotFoundException("expense")

        requireExpenseOwner(expenseToUpdate, currentUserId)
        requireExpenseStatusForUpdate(expenseToUpdate, updateExpenseStatusInput.expenseStatus)

        val updatedExpense = expenseToUpdate.copy(expenseStatus = updateExpenseStatusInput.expenseStatus)

        expenseRepository.updateExpense(updatedExpense)

        if (updateExpenseStatusInput.expenseStatus == ExpenseStatus.IN_PROGRESS_PAYING) {
            updateExpensePaymentsAmounts(updatedExpense)
        }
        if (updateExpenseStatusInput.expenseStatus == ExpenseStatus.DECLINED) {
            declineExpensePayments(updatedExpense)
        }

        return updatedExpense
    }

    // DELETE
    fun deleteExpense(expenseId: Long, currentUserId: Long): Expense {
        val expenseToDelete = expenseRepository.findExpenseById(expenseId)
                ?: throw EntityNotFoundException("expense")

        requireExpenseOwner(expenseToDelete, currentUserId)
        requireExpenseStatuses(expenseToDelete, listOf(ExpenseStatus.IN_PROGRESS_REQUESTING))

        expenseRepository.removeExpense(expenseToDelete)

        return expenseToDelete
    }

    private fun updateExpensePaymentsAmounts(updatedExpense: Expense) {
        val acceptedPayments = updatedExpense.payments.filter { it.status == PaymentStatus.ACCEPTED }
        val updatedAmount = updatedExpense.amount / (acceptedPayments.size + 1)

        paymentService.updatePaymentsAmount(acceptedPayments, updatedAmount)
    }

    private fun declineExpensePayments(expense: Expense) {
        paymentService.updatePaymentsStatuses(expense.payments.map { it.id }, PaymentStatus.DECLINED)
    }

    private fun requireExpenseStatusForUpdate(expense: Expense, statusToBeChangedTo: ExpenseStatus) {
        when (statusToBeChangedTo) {
            ExpenseStatus.IN_PROGRESS_REQUESTING -> {
                requireExpenseStatuses(expense, listOf(ExpenseStatus.DECLINED))
            }
            ExpenseStatus.IN_PROGRESS_PAYING -> {
                requireExpenseStatuses(expense, listOf(ExpenseStatus.IN_PROGRESS_REQUESTING))
                requirePaymentsStatuses(expense.payments, listOf(PaymentStatus.ACCEPTED, PaymentStatus.DECLINED))
            }
            ExpenseStatus.DECLINED -> {
                requireExpenseStatuses(expense, listOf(ExpenseStatus.IN_PROGRESS_REQUESTING))
                requirePaymentsStatuses(expense.payments, listOf(
                        PaymentStatus.IN_PROGRESS,
                        PaymentStatus.ACCEPTED,
                        PaymentStatus.DECLINED
                ))
            }
            ExpenseStatus.RESOLVED -> {
                requireExpenseStatuses(expense, listOf(ExpenseStatus.IN_PROGRESS_PAYING))
                requirePaymentsStatuses(expense.payments, listOf(PaymentStatus.CONFIRMED, PaymentStatus.DECLINED))
            }
        }
    }

    private fun requirePaymentsStatuses(payments: List<Payment>, availablePaymentStatuses: List<PaymentStatus>) {
        payments.forEach {
            if (!availablePaymentStatuses.contains(it.status)) throw PaymentStatusNotValid(it.status)
        }
    }

    private fun requirePartyParticipantsIncludeExpenseParticipants(
            expenseParticipants: Set<Long>,
            expenseParty: Party,
            currentUserId: Long
    ) {
        val partyParticipants = expenseParty.participants.map { it.id }.toSet()

        if (!partyParticipants.contains(currentUserId)) throw UnauthorisedException()
        if ((expenseParticipants - partyParticipants).isNotEmpty()) throw ExpenseParticipantNotInPartyException()
    }

    private fun requireExpenseStatuses(expenseToUpdate: Expense, statuses: List<ExpenseStatus>) {
        if (!statuses.contains(expenseToUpdate.expenseStatus))
            throw ExpenseStatusNotValid(expenseToUpdate.expenseStatus)
    }

}

fun NewExpenseInput.toDomain(userId: Long) = Expense(
        name = name,
        description = description,
        amount = amount,
        expenseDate = expenseDate,
        user = User(userId),
        party = if(this.partyId != null) Party(this.partyId.toLong()) else null
)

class ExpenseParticipantNotInPartyException : SimpleValidationException("Not all users were party participants")
class ExpenseStatusNotValid(status: ExpenseStatus) : SimpleValidationException("Expense status was not valid, status is $status")
class PaymentStatusNotValid(status: PaymentStatus) : SimpleValidationException("Payment status was not valid, status is $status")
