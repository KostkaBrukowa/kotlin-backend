package com.example.graphql.resolvers.partyrequest

import com.example.graphql.domain.partyrequest.PartyRequestDataLoaderService
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.DataFetcherOverride
import com.example.graphql.resolvers.utils.GQLResponseType
import com.example.graphql.resolvers.utils.dataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import com.expediagroup.graphql.annotations.GraphQLID
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

const val PARTY_REQUEST_PARTIES_LOADER_NAME = "PARTY_REQUEST_PARTIES_LOADER_NAME"
const val PARTY_REQUEST_RECEIVERS_LOADER_NAME = "PARTY_REQUEST_RECEIVERS_LOADER_NAME"

@Component
class PartyRequestDataLoadersBuilder(private val partyRequestDataLoaderService: PartyRequestDataLoaderService) {

    fun getReceiversDataLoader(): DataLoader<String, UserType> {
        return dataLoader { ids -> partyRequestDataLoaderService.partyRequestToUserDataLoaderMap(ids) }
    }

    fun getPartiesDataLoader(): DataLoader<String, PartyType> {
        return dataLoader { ids -> partyRequestDataLoaderService.partyRequestToPartyDataLoaderMap(ids) }
    }
}

@Component("PartyRequestReceiverDataFetcher")
@Scope("prototype")
class PartyRequestReceiverDataFetcher : DataFetcherOverride<PartyRequestType, UserType>(
        PARTY_REQUEST_RECEIVERS_LOADER_NAME
)

@Component("PartyRequestPartyDataFetcher")
@Scope("prototype")
class PartyRequestPartyDataFetcher : DataFetcherOverride<PartyRequestType, UserType>(
        PARTY_REQUEST_PARTIES_LOADER_NAME
)

