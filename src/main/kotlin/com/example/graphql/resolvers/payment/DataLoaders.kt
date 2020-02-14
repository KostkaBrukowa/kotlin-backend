package com.example.graphql.resolvers.payment

import com.example.graphql.domain.partyrequest.PartyRequestDataLoaderService
import com.example.graphql.domain.payment.PaymentDataLoaderService
import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.DataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


const val PAYMENT_PAYER_LOADER_NAME = "PARTY_REQUEST_PARTIES_LOADER_NAME"
const val PAYMENT_EXPENSE_LOADER_NAME = "PARTY_REQUEST_RECEIVERS_LOADER_NAME"

@Component
class PartyRequestDataLoadersBuilder(private val paymentDataLoaderService: PaymentDataLoaderService) {

    fun getPayersDataLoader(): DataLoader<String, UserType> {
        return dataLoader { ids -> paymentDataLoaderService.paymentToUserDataLoaderMap(ids) }
    }

    fun getExpensesDataLoader(): DataLoader<String, ExpenseType> {
        return dataLoader { ids -> paymentDataLoaderService.paymentToExpenseDataLoaderMap(ids) }
    }
}

@Component("PartyRequestReceiverDataFetcher")
@Scope("prototype")
class PartyRequestReceiverDataFetcher : DataFetcher<PaymentType, UserType>(
        PAYMENT_EXPENSE_LOADER_NAME
)

@Component("PartyRequestPartyDataFetcher")
@Scope("prototype")
class PartyRequestPartyDataFetcher : DataFetcher<PaymentType, UserType>(
        PAYMENT_PAYER_LOADER_NAME
)

