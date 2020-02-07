package com.example.graphql.resolvers.party

import com.example.graphql.domain.party.PartyDataLoaderService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.DataFetcherOverride
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

//1. Name of DataFetcher same as class lazyinit val
//2. .getDataLoader<String, List<PartyRequest>>("TU NAZWA TAKA SAMA JAK W DATA LOADER CONFIGURATION")
//3. Typy <String, List<Typ>

const val PARTY_PARTY_REQUEST_LOADER_NAME = "partyPartyRequestLoader"
const val PARTY_PARTICIPANTS_LOADER_NAME = "partyParticipantsLoader"

@Component
class PartyDataLoaderBuilder(private val partyDataLoaderService: PartyDataLoaderService) {

    fun getPartyRequestsDataLoader(): DataLoader<String, List<PartyRequestType>> {
        return dataLoader { ids -> partyDataLoaderService.partyToPartyRequestsDataLoaderMap(ids) }
    }

    fun getParticipantsDataLoader(): DataLoader<String, List<UserType>> {
        return dataLoader { ids -> partyDataLoaderService.partyToParticipantsDataLoaderMap(ids) }
    }
}

@Component("PartyPartyRequestsDataFetcher")
@Scope("prototype")
class PartyPartyRequestsDataFetcher : DataFetcherOverride<PartyType, List<PartyRequestType>>(
        PARTY_PARTY_REQUEST_LOADER_NAME
)

@Component("PartyParticipantsDataFetcher")
@Scope("prototype")
class PartyParticipantsDataFetcher : DataFetcherOverride<PartyType, List<UserType>>(
        PARTY_PARTICIPANTS_LOADER_NAME
)
