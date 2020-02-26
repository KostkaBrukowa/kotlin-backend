package com.example.graphql.domain.party

import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.expense.toResponse
import com.example.graphql.resolvers.message.MessageResponseType
import com.example.graphql.resolvers.message.MessageType
import com.example.graphql.resolvers.message.toResponse
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.partyrequest.toResponse
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.user.toResponse
import org.springframework.stereotype.Component

@Component
class PartyDataLoaderService(private val partyRepository: PartyRepository) {

    fun partyToPartyRequestsDataLoaderMap(partiesIds: Set<Long>): Map<Long, List<PartyRequestType>> {
        val parties = partyRepository.findPartiesWithParticipants(partiesIds)

        return parties.associateBy({ it.id }, { it.partyRequests.map { participant -> participant.toResponse() } })
    }


    fun partyToParticipantsDataLoaderMap(partiesIds: Set<Long>): Map<Long, List<UserType>> {
        val parties = partyRepository.findPartiesWithParticipants(partiesIds)

        return parties.associateBy({ it.id }, { it.participants.map { participant -> participant.toResponse() } })
    }

    fun partyToExpensesDataLoaderMap(ids: Set<Long>): Map<Long, List<ExpenseType>> {
        val parties = partyRepository.findPartiesWithExpenses(ids)

        return parties.associateBy({ it.id }, { it.expenses.map { expense -> expense.toResponse() } })
    }

    fun partyToMessagesDataLoaderMap(ids: Set<Long>): Map<Long, List<MessageResponseType>> {
        val messages = partyRepository.findPartiesWithMessages(ids)

        return messages.map { it.key.id to it.value.map {message -> message.toResponse() } }.toMap()
    }
}
