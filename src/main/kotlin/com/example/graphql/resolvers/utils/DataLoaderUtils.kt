package com.example.graphql.resolvers.utils

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture

fun <T>dataLoader(
        providerFunction: (ids: Set<Long>) -> Map<Long, T>
): DataLoader<String, T>  {
    val mapBatchLoader: MappedBatchLoader<String, T> = MappedBatchLoader { ids ->
        CompletableFuture.supplyAsync {
            val userToRequestsMap = providerFunction(ids.map { it.toLong() }.toSet())

            userToRequestsMap.mapKeys { it.key.toString() }
        }
    }

    return DataLoader.newMappedDataLoader(mapBatchLoader)
}

abstract class DataFetcher(
        private val loaderName: String
) : DataFetcher<CompletableFuture<GQLResponseType>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<GQLResponseType> {
        val id = environment.getSource<GQLResponseType>().id

        return environment
                .getDataLoader<String, GQLResponseType>(loaderName)
                .load(id)
    }
}
