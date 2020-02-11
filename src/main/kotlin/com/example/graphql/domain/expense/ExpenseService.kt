package com.example.graphql.domain.expense

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.party.PartyService
import com.example.graphql.domain.payment.PaymentService
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.expense.NewExpenseInput
import com.example.graphql.schema.exceptions.handlers.SimpleValidationException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class ExpenseService(
        private val expenseRepository: ExpenseRepository,
        private val partyService: PartyService,
        private val paymentService: PaymentService
) {

    fun createExpense(expenseInput: NewExpenseInput, currentUserId: Long): Expense {
        val expenseParty = partyService.findPartiesWithParticipants(setOf(expenseInput.partyId)).firstOrNull()
                ?: throw PartyNotFoundException()

        val expenseParticipants = expenseInput.participants.filter { it != currentUserId }.toSet()

        requirePartyParticipantsIncludeExpenseParticipants(expenseParticipants, expenseParty, currentUserId)

        val newExpense = expenseRepository.saveNewExpense(expenseInput.toDomain(currentUserId))

//        paymentService.createPaymentsForExpense(newExpense, expenseParticipants) // TODO uncomment when payment service is done

        return newExpense
    }

    private fun requirePartyParticipantsIncludeExpenseParticipants(
            expenseParticipants: Set<Long>,
            expenseParty: Party,
            currentUserId: Long
    ) {
        val partyParticipants = expenseParty.participants.map { it.id }.toSet()

        if ((expenseParticipants - partyParticipants).isNotEmpty()) throw ExpenseParticipantNotInPartyException()
        if (!partyParticipants.contains(currentUserId)) throw UnauthorisedException()
    }
}

fun NewExpenseInput.toDomain(userId: Long) = Expense(
        description = description,
        amount = amount,
        expenseDate = expenseDate,
        user = User(userId),
        party = Party(this.partyId)
)

class PartyNotFoundException : SimpleValidationException("Party with such id was not found")
class ExpenseParticipantNotInPartyException : SimpleValidationException("Not all users were party participants")
