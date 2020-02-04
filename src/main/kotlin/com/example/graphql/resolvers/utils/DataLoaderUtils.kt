package com.example.graphql.resolvers.utils

import graphql.schema.DataFetchingEnvironment
import org.dataloader.DataLoader
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture

fun <T>dataLoader(
        providerFunction: (ids: Set<Long>) -> Map<Long, List<T>>
): DataLoader<String, List<T>>  {
    val mapBatchLoader: MappedBatchLoader<String, List<T>> = MappedBatchLoader { ids ->
        CompletableFuture.supplyAsync {
            val userToRequestsMap = providerFunction(ids.map { it.toLong() }.toSet())

            userToRequestsMap.mapKeys { it.key.toString() }
        }
    }

    return DataLoader.newMappedDataLoader(mapBatchLoader)
}

fun <Source, Result> dataFetcher(
        loaderName: String,
        environment: DataFetchingEnvironment,
        idExtractor: (model: Source) -> String
): CompletableFuture<List<Result>> {
    val id = idExtractor(environment.getSource<Source>())

    return environment
            .getDataLoader<String, List<Result>>(loaderName)
            .load(id)
}
