package com.example.graphql.domain.expense

import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.domain.payment.PaymentStatus
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.resolvers.expense.NewExpenseInput
import com.example.graphql.resolvers.expense.UpdateExpenseAmountInput
import com.example.graphql.resolvers.expense.UpdateExpenseInput
import com.example.graphql.resolvers.expense.UpdateExpenseStatusInput
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.SimpleValidationException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class ExpenseService(
        private val expenseRepository: ExpenseRepository,
        private val paymentService: PaymentService,
        private val userRepository: UserRepository,
        private val partyRepository: PartyRepository,
        private val notificationService: NotificationService
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
        val expenseParty = partyRepository.findPartiesWithParticipants(setOf(expenseInput.partyId.toLong())).firstOrNull()
                ?: throw EntityNotFoundException("party")

        val expenseParticipants = expenseInput.participants.filter { it.toLong() != currentUserId }.map { it.toLong() }.toSet()

        requirePartyParticipantsIncludeExpenseParticipants(expenseParticipants.map { it }.toSet(), expenseParty, currentUserId)

        val newExpense = expenseRepository.saveNewExpense(expenseInput.toDomain(currentUserId))

        paymentService.createPaymentsForExpense(newExpense, expenseParticipants)

        return newExpense
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
        if(expenseToUpdate.amount == amount)
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
    fun deleteExpense(expenseId: Long, currentUserId: Long): Boolean {
        val expenseToDelete = expenseRepository.findExpenseById(expenseId)
                ?: throw EntityNotFoundException("expense")

        requireExpenseOwner(expenseToDelete, currentUserId)
        requireExpenseStatuses(expenseToDelete, listOf(ExpenseStatus.IN_PROGRESS_REQUESTING))

        expenseRepository.removeExpense(expenseToDelete)

        return true
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
        party = Party(this.partyId.toLong())
)

class ExpenseParticipantNotInPartyException : SimpleValidationException("Not all users were party participants")
class ExpenseStatusNotValid(status: ExpenseStatus) : SimpleValidationException("Expense status was not valid, status is $status")
class PaymentStatusNotValid(status: PaymentStatus) : SimpleValidationException("Payment status was not valid, status is $status")
