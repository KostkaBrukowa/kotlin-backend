package com.example.graphql.domain.payment

import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.expense.toResponse
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class PaymentDataLoaderService(private val paymentRepository: PaymentRepository) {

    fun paymentToUserDataLoaderMap(ids: Set<Long>): Map<Long, UserType> {
        val partyRequests = paymentRepository.findPaymentsWithUsers(ids)

        return partyRequests.associateBy({ it.id }, { it.user!!.toResponse() })
    }

    fun paymentToExpenseDataLoaderMap(ids: Set<Long>): Map<Long, ExpenseType> {
        val partyRequests = paymentRepository.findPaymentsWithExpenses(ids)

        return partyRequests.associateBy({ it.id }, { it.expense!!.toResponse() })
    }
}
