package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.PartyDataLoaderService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.DataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

const val PARTY_PARTY_REQUEST_LOADER_NAME = "PartyPartyRequestsDataFetcher"
const val PARTY_PARTICIPANTS_LOADER_NAME = "PartyParticipantsDataFetcher"

@Component
class PartyDataLoaderBuilder(private val partyDataLoaderService: PartyDataLoaderService) {

    fun getPartyRequestsDataLoader(): DataLoader<String, List<PartyRequestType>> {
        return dataLoader { ids -> partyDataLoaderService.partyToPartyRequestsDataLoaderMap(ids) }
    }

    fun getParticipantsDataLoader(): DataLoader<String, List<UserType>> {
        return dataLoader { ids -> partyDataLoaderService.partyToParticipantsDataLoaderMap(ids) }
    }
}

@Component(PARTY_PARTY_REQUEST_LOADER_NAME)
@Scope("prototype")
class PartyPartyRequestsDataFetcher : DataFetcher(PARTY_PARTY_REQUEST_LOADER_NAME)

@Component(PARTY_PARTICIPANTS_LOADER_NAME)
@Scope("prototype")
class PartyParticipantsDataFetcher : DataFetcher(PARTY_PARTICIPANTS_LOADER_NAME)
