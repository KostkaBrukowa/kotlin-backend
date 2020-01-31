package com.example.graphql.resolvers.party

import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.dataloader.MappedBatchLoader
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

//1. Name of DataFetcher same as class lazyinit val
//2. .getDataLoader<String, List<PartyRequest>>("TU NAZWA TAKA SAMA JAK W DATA LOADER CONFIGURATION")
//3. Typy <String, List<Typ>

const val PARTY_PARTY_REQUEST_LOADER_NAME = "partyPartyRequestLoader"
const val PARTY_PARTICIPANTS_LOADER_NAME = "partyParticipantsLoader"

@Component
class PartyDataLoaderBuilder(
        private val partyRequestService: PartyRequestService,
        private val userService: UserService
) {

    fun getPartyRequestDataLoader(): DataLoader<String, List<PartyRequestType>> {
        val mapBatchLoader = MappedBatchLoader<String, List<PartyRequestType>> { userIds ->
            CompletableFuture.supplyAsync { partyRequestService.findAllByPartiesIds(userIds) }
        }

        return DataLoader.newMappedDataLoader(mapBatchLoader)
    }

    fun getParticipantsDataLoader(): DataLoader<String, List<UserType>> {
        val mapBatchLoader = MappedBatchLoader<String, List<UserType>> { userIds ->
            CompletableFuture.supplyAsync { userService.findAllParticipantsByPartiesIds(userIds) }
        }

        return DataLoader.newMappedDataLoader(mapBatchLoader)
    }
}

@Component("partyPartyRequestsDataFetcher")
@Scope("prototype")
class PartyPartyRequestDataFetcher : DataFetcher<CompletableFuture<List<PartyRequest>>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<PartyRequest>> {
        val partyId = environment.getSource<PartyType>().id

        return environment
                .getDataLoader<String, List<PartyRequest>>(PARTY_PARTY_REQUEST_LOADER_NAME)
                .load(partyId)
    }
}

@Component("partyParticipantsDataFetcher")
@Scope("prototype")
class PartyParticipantsDataFetcher : DataFetcher<CompletableFuture<List<User>>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<User>> {
        val partyId = environment.getSource<PartyType>().id

        return environment
                .getDataLoader<String, List<User>>(PARTY_PARTICIPANTS_LOADER_NAME)
                .load(partyId)
    }
}
