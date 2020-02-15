package com.example.graphql.domain.user

import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.expense.toResponse
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.party.toResponse
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import com.example.graphql.resolvers.payment.PaymentType
import com.example.graphql.resolvers.payment.toResponse
import org.springframework.stereotype.Component

@Component
class UserDataLoaderService(private val userRepository: UserRepository) {

    fun userToPartyRequestsDataLoaderMap(userIds: Set<Long>): Map<Long, List<PartyRequestType>> {
        val users = userRepository.findUsersWithPartyRequests(userIds)

        return users.associateBy({ it.id }, { it.partyRequests.map { participant -> participant.toResponse() } })
    }

    fun userToJoinedPartiesDataLoaderMap(ids: Set<Long>): Map<Long, List<PartyType>> {
        val users = userRepository.findUsersWithJoinedParties(ids)

        return users.associateBy({ it.id }, { it.joinedParties.map { party -> party.toResponse() } })
    }

    fun userToPaymentsDataLoaderMap(ids: Set<Long>): Map<Long, List<PaymentType>> {
        val users = userRepository.findUsersWithPayments(ids)

        return users.associateBy({ it.id }, { it.payments.map { payment -> payment.toResponse() } })
    }

    fun userToExpensesDataLoaderMap(ids: Set<Long>): Map<Long, List<ExpenseType>> {
        val users = userRepository.findUsersWithExpenses(ids)

        return users.associateBy({ it.id }, { it.expenses.map { expense -> expense.toResponse() } })
    }
}
