package com.example.graphql.resolvers.expense

import com.example.graphql.domain.expense.ExpenseDataLoaderService
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.DataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

const val EXPENSE_PARTY_LOADER_NAME = "EXPENSE_PARTY_LOADER_NAME"
const val EXPENSE_PAYER_LOADER_NAME = "EXPENSE_PAYER_LOADER_NAME"

@Component
class ExpenseDataLoaderBuilder(private val expenseDataLoaderService: ExpenseDataLoaderService) {

    fun getPartyDataLoader(): DataLoader<String, PartyType> {
        return dataLoader { ids -> expenseDataLoaderService.expenseToPartyDataLoaderMap(ids) }
    }

    fun getPayerDataLoader(): DataLoader<String, UserType> {
        return dataLoader { ids -> expenseDataLoaderService.expenseToPayerDataLoaderMap(ids) }
    }
}

@Component("ExpensePartyDataFetcher")
@Scope("prototype")
class ExpensePartyDataFetcher : DataFetcher<ExpenseType, PartyType>(
        EXPENSE_PARTY_LOADER_NAME
)

@Component("ExpensePayerDataFetcher")
@Scope("prototype")
class ExpensePayerDataFetcher : DataFetcher<PartyType, UserType>(
        EXPENSE_PAYER_LOADER_NAME
)
