package com.example.graphql.resolvers.configuration

import com.example.graphql.resolvers.expense.EXPENSE_PARTY_LOADER_NAME
import com.example.graphql.resolvers.expense.EXPENSE_PAYER_LOADER_NAME
import com.example.graphql.resolvers.expense.ExpenseDataLoaderBuilder
import com.example.graphql.resolvers.party.PARTY_PARTICIPANTS_LOADER_NAME
import com.example.graphql.resolvers.party.PARTY_PARTY_REQUEST_LOADER_NAME
import com.example.graphql.resolvers.party.PartyDataLoaderBuilder
import com.example.graphql.resolvers.partyrequest.PARTY_REQUEST_PARTIES_LOADER_NAME
import com.example.graphql.resolvers.partyrequest.PARTY_REQUEST_RECEIVERS_LOADER_NAME
import com.example.graphql.resolvers.partyrequest.PartyRequestDataLoadersBuilder
import com.example.graphql.resolvers.payment.PAYMENT_EXPENSE_LOADER_NAME
import com.example.graphql.resolvers.payment.PAYMENT_PAYER_LOADER_NAME
import com.example.graphql.resolvers.payment.PaymentDataLoadersBuilder
import com.example.graphql.resolvers.user.USER_PARTY_REQUEST_LOADER_NAME
import com.example.graphql.resolvers.user.UserDataLoaderBuilder
import com.expediagroup.graphql.spring.execution.DataLoaderRegistryFactory
import org.dataloader.DataLoaderRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class DataLoaderConfiguration(
        private val userDataLoaderBuilder: UserDataLoaderBuilder,
        private val partyDataLoaderBuilder: PartyDataLoaderBuilder,
        private val partyRequestDataLoadersBuilder: PartyRequestDataLoadersBuilder,
        private val expenseDataLoaderBuilder: ExpenseDataLoaderBuilder,
        private val paymentDataLoadersBuilder: PaymentDataLoadersBuilder
) {

    @Bean
    fun dataLoaderRegistryFactory(): DataLoaderRegistryFactory {
        return object : DataLoaderRegistryFactory {
            override fun generate(): DataLoaderRegistry {
                val registry = DataLoaderRegistry()

                registry.register(USER_PARTY_REQUEST_LOADER_NAME, userDataLoaderBuilder.getPartyRequestDataLoader())
                registry.register(PARTY_PARTICIPANTS_LOADER_NAME, partyDataLoaderBuilder.getParticipantsDataLoader())
                registry.register(PARTY_PARTY_REQUEST_LOADER_NAME, partyDataLoaderBuilder.getPartyRequestsDataLoader())
                registry.register(PARTY_REQUEST_PARTIES_LOADER_NAME, partyRequestDataLoadersBuilder.getPartiesDataLoader())
                registry.register(PARTY_REQUEST_RECEIVERS_LOADER_NAME, partyRequestDataLoadersBuilder.getReceiversDataLoader())
                registry.register(EXPENSE_PAYER_LOADER_NAME, expenseDataLoaderBuilder.getPayerDataLoader())
                registry.register(EXPENSE_PARTY_LOADER_NAME, expenseDataLoaderBuilder.getPartyDataLoader())
                registry.register(PAYMENT_EXPENSE_LOADER_NAME, paymentDataLoadersBuilder.getExpensesDataLoader())
                registry.register(PAYMENT_PAYER_LOADER_NAME, paymentDataLoadersBuilder.getPayersDataLoader())

                return registry
            }
        }
    }
}
