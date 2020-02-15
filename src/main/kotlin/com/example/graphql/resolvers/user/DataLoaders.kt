package com.example.graphql.resolvers.user

import com.example.graphql.domain.user.UserDataLoaderService
import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.payment.PaymentType
import com.example.graphql.resolvers.utils.DataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

const val USER_PARTY_REQUEST_LOADER_NAME = "UserPartyRequestsDataFetcher"
const val USER_JOINED_PARTIES_LOADER_NAME = "UserJoinedPartiesDataFetcher"
const val USER_PAYMENTS_LOADER_NAME = "UserPaymentsDataFetcher"
const val USER_EXPENSES_LOADER_NAME = "UserExpensesDataFetcher"

@Component
class UserDataLoaderBuilder(private val userDataLoaderService: UserDataLoaderService) {

    fun getPartyRequestDataLoader(): DataLoader<String, List<PartyRequestType>> {
        return dataLoader { ids -> userDataLoaderService.userToPartyRequestsDataLoaderMap(ids) }
    }

    fun getJoinedPartiesDataLoader(): DataLoader<String, List<PartyType>> {
        return dataLoader { ids -> userDataLoaderService.userToJoinedPartiesDataLoaderMap(ids) }
    }

    fun getPaymentsDataLoader(): DataLoader<String, List<PaymentType>> {
        return dataLoader { ids -> userDataLoaderService.userToPaymentsDataLoaderMap(ids) }
    }

    fun getExpensesDataLoader(): DataLoader<String, List<ExpenseType>> {
        return dataLoader { ids -> userDataLoaderService.userToExpensesDataLoaderMap(ids) }
    }
}

@Component(USER_PARTY_REQUEST_LOADER_NAME)
@Scope("prototype")
class UserPartyRequestsDataFetcher : DataFetcher(USER_PARTY_REQUEST_LOADER_NAME)

@Component(USER_JOINED_PARTIES_LOADER_NAME)
@Scope("prototype")
class UserJoinedPartiesDataFetcher : DataFetcher(USER_JOINED_PARTIES_LOADER_NAME)

@Component(USER_PAYMENTS_LOADER_NAME)
@Scope("prototype")
class UserPaymentsDataFetcher : DataFetcher(USER_PAYMENTS_LOADER_NAME)

@Component(USER_EXPENSES_LOADER_NAME)
@Scope("prototype")
class UserExpensesDataFetcher : DataFetcher(USER_EXPENSES_LOADER_NAME)
