package com.example.graphql.resolvers.message

import com.example.graphql.configuration.context.AppGraphQLContext
import com.example.graphql.domain.message.MessageService
import com.example.graphql.resolvers.expense.NewExpenseInput
import com.example.graphql.resolvers.expense.toResponse
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.annotations.GraphQLContext
import com.expediagroup.graphql.spring.operations.Mutation
import org.springframework.stereotype.Component
import javax.validation.Valid

@Component
class MessageMutation(private val messageService: MessageService): Mutation {

    @Authenticated(role = Roles.USER)
    fun createMessage(
            @Valid newMessageInput: NewMessageInput,
            @GraphQLContext context: AppGraphQLContext
    ) = messageService.addMessage(newMessageInput, context.subject).toResponse()

    @Authenticated(role = Roles.USER)
    fun removeMessage(
            messageId: Long,
            messageType: MessageType,
            @GraphQLContext context: AppGraphQLContext
    ) = messageService.removeMessage(messageId, messageType, context.subject).toResponse()
}

enum class MessageType {
    PARTY,
    PAYMENT,
    BULK_PAYMENT,
    EXPENSE
}
