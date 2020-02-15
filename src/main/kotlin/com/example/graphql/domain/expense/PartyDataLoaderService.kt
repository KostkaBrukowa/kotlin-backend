package com.example.graphql.domain.expense

import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.party.toResponse
import com.example.graphql.resolvers.payment.PaymentType
import com.example.graphql.resolvers.payment.toResponse
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class ExpenseDataLoaderService(private val expenseRepository: ExpenseRepository) {

    fun expenseToPartyDataLoaderMap(partiesIds: Set<Long>): Map<Long, PartyType> {
        val parties = expenseRepository.findExpensesWithParties(partiesIds)

        return parties.associateBy({ it.id }, { it.party!!.toResponse() })
    }


    fun expenseToPayerDataLoaderMap(partiesIds: Set<Long>): Map<Long, UserType> {
        val parties = expenseRepository.findExpensesWithPayers(partiesIds)

        return parties.associateBy({ it.id }, { it.user!!.toResponse() })
    }

    fun expenseToPaymentsDataLoaderMap(ids: Set<Long>): Map<Long, List<PaymentType>> {
        val parties = expenseRepository.findExpensesWithPayments(ids)

        return parties.associateBy({ it.id }, { it.payments.map { payment -> payment.toResponse() } })
    }
}
