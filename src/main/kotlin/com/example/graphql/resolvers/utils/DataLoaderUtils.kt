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

fun <Source : GQLResponseType, Result> dataFetcher(
        loaderName: String,
        environment: DataFetchingEnvironment
): CompletableFuture<Result> {
    val id = environment.getSource<Source>().id

    return environment
            .getDataLoader<String, Result>(loaderName)
            .load(id)
}

abstract class DataFetcherOverride<Source : GQLResponseType, Result>(
        private val loaderName: String
) : DataFetcher<CompletableFuture<Result>> {

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<Result> {
        return dataFetcher<Source, Result>(loaderName, environment)
    }
}
