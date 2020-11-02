package com.example.graphql.schema.exceptions

import ValidationGraphQLError
import asValidationDataFetchingGraphQLError
import com.example.graphql.schema.exceptions.handlers.*
import com.expediagroup.graphql.spring.exception.SimpleKotlinGraphQLError
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.slf4j.LoggerFactory
import java.lang.reflect.UndeclaredThrowableException
import javax.validation.ConstraintViolationException

class CustomDataFetcherExceptionHandler : DataFetcherExceptionHandler {
    private val log = LoggerFactory.getLogger(CustomDataFetcherExceptionHandler::class.java)

    override fun onException(handlerParameters: DataFetcherExceptionHandlerParameters): DataFetcherExceptionHandlerResult {
        val exception = handlerParameters.exception
        val sourceLocation = handlerParameters.sourceLocation
        val path = handlerParameters.path

        val error: GraphQLError = when (exception) {
            is ValidationException -> ValidationGraphQLError(exception.constraintErrors, path, exception, sourceLocation)
            is ConstraintViolationException -> exception.asValidationDataFetchingGraphQLError(exception, path, sourceLocation)
            is UnauthenticatedException -> UnauthenticatedGraphQLError(exception, path, sourceLocation)
            is SimpleValidationException -> SimpleValidationGraphQLError(exception, path, sourceLocation)
            is UndeclaredThrowableException -> SimpleValidationGraphQLError(exception.undeclaredThrowable, path, sourceLocation)
            else -> SimpleKotlinGraphQLError(exception = exception, locations = listOf(sourceLocation), path = path.toList())
        }
        log.warn(error.message, exception)
        return DataFetcherExceptionHandlerResult.newResult().error(error).build()
    }
}

