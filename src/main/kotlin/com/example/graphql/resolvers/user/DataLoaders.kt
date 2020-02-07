package com.example.graphql.resolvers.user

import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.UserDataLoaderService
import com.example.graphql.resolvers.partyrequest.PARTY_REQUEST_RECEIVERS_LOADER_NAME
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.utils.DataFetcherOverride
import com.example.graphql.resolvers.utils.dataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

const val USER_PARTY_REQUEST_LOADER_NAME = "userPartyRequestLoader"

@Component
class UserDataLoaderBuilder(private val userDataLoaderService: UserDataLoaderService) {

    fun getPartyRequestDataLoader(): DataLoader<String, List<PartyRequestType>> {
        return dataLoader { ids -> userDataLoaderService.userToPartyRequestsDataLoaderMap(ids) }
    }
}

@Component("UserPartyRequestsDataFetcher")
@Scope("prototype")
class UserPartyRequestsDataFetcher : DataFetcherOverride<UserType, List<PartyRequestType>>(
        USER_PARTY_REQUEST_LOADER_NAME
)
