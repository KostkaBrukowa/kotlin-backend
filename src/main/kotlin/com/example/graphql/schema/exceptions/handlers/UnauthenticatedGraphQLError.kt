package com.example.graphql.schema.exceptions.handlers

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import graphql.ErrorType
import graphql.ExceptionWhileDataFetching
import graphql.execution.ExecutionPath
import graphql.language.SourceLocation

@JsonIgnoreProperties("exception")
class UnauthenticatedGraphQLError(
        exception: Throwable,
        path: ExecutionPath,
        sourceLocation: SourceLocation
) : ExceptionWhileDataFetching(
        path,
        exception,
        sourceLocation
) {
    override fun getErrorType(): ErrorType = ErrorType.ExecutionAborted
}
