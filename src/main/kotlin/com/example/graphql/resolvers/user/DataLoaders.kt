package com.example.graphql.resolvers.user

import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.dataloader.MappedBatchLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

const val USER_PARTY_REQUEST_LOADER_NAME = "userPartyRequestLoader"

@Component
class UserDataLoaderBuilder(private val partyRequestService: PartyRequestService) {

    fun getPartyRequestDataLoader(): DataLoader<String, List<PartyRequestType>> {
        val mapBatchLoader: MappedBatchLoader<String, List<PartyRequestType>> = MappedBatchLoader { userIds ->
            CompletableFuture.supplyAsync { partyRequestService.findAllByUsersIds(userIds) }
        }

        return DataLoader.newMappedDataLoader(mapBatchLoader)
    }
}

@Component("userPartyRequestsDataFetcher")
@Scope("prototype")
class UserPartyRequestsDataFetcher : DataFetcher<CompletableFuture<List<PartyRequest>>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<PartyRequest>> {
        val userId = environment.getSource<UserType>().id

        return environment
                .getDataLoader<String, List<PartyRequest>>(USER_PARTY_REQUEST_LOADER_NAME)
                .load(userId)
    }
}
