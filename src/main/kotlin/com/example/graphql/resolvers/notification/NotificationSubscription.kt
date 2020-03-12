package com.example.graphql.resolvers.notification

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Subscription
import reactor.core.publisher.Flux
import java.time.Duration
import kotlin.random.Random

class NotificationSubscription : Subscription {

    @Authenticated(role = Roles.USER)
    fun counterWithError(
            userId: Long,
            @GraphQLContext context: AppGraphQLContext
    ): Flux<Int> = Flux.interval(Duration.ofSeconds(1)).map { Random.nextInt() }
}
