package com.example.graphql.resolvers.user

import com.example.graphql.domain.user.UserDataLoaderService
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.utils.DataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

const val USER_PARTY_REQUEST_LOADER_NAME = "userPartyRequestLoader"

@Component
class UserDataLoaderBuilder(private val userDataLoaderService: UserDataLoaderService) {

    fun getPartyRequestDataLoader(): DataLoader<String, List<PartyRequestType>> {
        return dataLoader { ids -> userDataLoaderService.userToPartyRequestsDataLoaderMap(ids) }
    }
}

@Component("UserPartyRequestsDataFetcher")
@Scope("prototype")
class UserPartyRequestsDataFetcher : DataFetcher<UserType, List<PartyRequestType>>(
        USER_PARTY_REQUEST_LOADER_NAME
)
