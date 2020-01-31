package com.example.graphql.resolvers.utils

fun <T, ResultType> dataLoaderGrouping(
        ids: Set<String>,
        entities: List<T>,
        groupBucketExtractor: (entity: T) -> String?,
        transformer: (entity: T) -> ResultType
): Map<String, List<ResultType>> {

    val idsToEmptyLists = idsToEmptyList<ResultType>(ids)

    return entities.fold(idsToEmptyLists) { acc, entity ->
        val bucket = groupBucketExtractor(entity) ?: return@fold acc //TODO POPRAWIc
        val entitiesList = acc[bucket] ?: return@fold acc

        acc + (bucket to entitiesList + transformer(entity))
    }
}

fun <T, K, ResultType> dataLoaderGroupingList(
        ids: Set<String>,
        entities: List<T>,
        groupBucketExtractor: (entity: K) -> String?,
        arrayExtractor: (entity: T) -> List<K>,
        transformer: (entity: T) -> ResultType
): Map<String, List<ResultType>> {

    val idsToEmptyLists = idsToEmptyList<ResultType>(ids)

    return entities.fold(idsToEmptyLists) { acc, entity ->
        arrayExtractor(entity).fold(acc) innerFold@{ innerAcc, innerEntity ->
            val entityId = groupBucketExtractor(innerEntity) ?: return@innerFold innerAcc
            val entitiesList = acc[entityId] ?: return@innerFold innerAcc

            innerAcc + (entityId to entitiesList + transformer(entity))
        }
    }
}

private fun <T> idsToEmptyList(ids: Set<String>): Map<String, List<T>> {
    return ids.fold(mapOf()) { acc, id -> acc + (id to emptyList()) }
}
