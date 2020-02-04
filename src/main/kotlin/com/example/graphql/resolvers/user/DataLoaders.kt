package com.example.graphql.resolvers.user

import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
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
class UserDataLoaderBuilder(private val partyRequestService: PartyRequestService) {

    fun getPartyRequestDataLoader(): DataLoader<String, List<PartyRequestType>> {
        return dataLoader { ids -> partyRequestService.findAllByUsersIds(ids) }
    }
}

@Component("userPartyRequestsDataFetcher")
@Scope("prototype")
class UserPartyRequestsDataFetcher : DataFetcher<CompletableFuture<List<PartyRequestType>>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<PartyRequestType>> {
        return dataFetcher<UserType, PartyRequestType>(USER_PARTY_REQUEST_LOADER_NAME, environment) { it.id }
    }
}
