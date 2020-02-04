package com.example.graphql.resolvers.party

import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.UserService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.dataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
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
        return dataLoader { ids -> partyRequestService.findAllByPartiesIds(ids) }
    }

    fun getParticipantsDataLoader(): DataLoader<String, List<UserType>> {
        return dataLoader { ids -> userService.findAllParticipantsByPartiesIds(ids) }
    }
}

@Component("partyPartyRequestsDataFetcher")
@Scope("prototype")
class PartyPartyRequestDataFetcher : DataFetcher<CompletableFuture<List<PartyRequestType>>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<PartyRequestType>> {
        return dataFetcher<PartyType, PartyRequestType>(PARTY_PARTY_REQUEST_LOADER_NAME, environment) { it.id }
    }
}

@Component("partyParticipantsDataFetcher")
@Scope("prototype")
class PartyParticipantsDataFetcher : DataFetcher<CompletableFuture<List<UserType>>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<UserType>> {
        return dataFetcher<PartyType, UserType>(PARTY_PARTICIPANTS_LOADER_NAME, environment) { it.id }
    }
}
