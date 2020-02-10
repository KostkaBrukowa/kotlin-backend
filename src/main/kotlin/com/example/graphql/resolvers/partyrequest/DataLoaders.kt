package com.example.graphql.resolvers.partyrequest

import com.example.graphql.domain.partyrequest.PartyRequestDataLoaderService
import com.example.graphql.resolvers.party.PartyType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.DataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

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
class PartyRequestReceiverDataFetcher : DataFetcher<PartyRequestType, UserType>(
        PARTY_REQUEST_RECEIVERS_LOADER_NAME
)

@Component("PartyRequestPartyDataFetcher")
@Scope("prototype")
class PartyRequestPartyDataFetcher : DataFetcher<PartyRequestType, UserType>(
        PARTY_REQUEST_PARTIES_LOADER_NAME
)

