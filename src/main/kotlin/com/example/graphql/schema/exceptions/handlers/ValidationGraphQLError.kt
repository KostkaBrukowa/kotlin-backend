import com.example.graphql.schema.exceptions.handlers.ConstraintError
import com.example.graphql.schema.exceptions.handlers.asConstraintError
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import graphql.ErrorType
import graphql.ExceptionWhileDataFetching
import graphql.execution.ExecutionPath
import graphql.language.SourceLocation
import javax.validation.ConstraintViolationException

@JsonIgnoreProperties("exception")
class ValidationGraphQLError(
        val constraintErrors: List<ConstraintError>,
        path: ExecutionPath,
        exception: Throwable,
        sourceLocation: SourceLocation
) : ExceptionWhileDataFetching(
        path,
        exception,
        sourceLocation
) {
    override fun getErrorType(): ErrorType = ErrorType.ValidationError
}

fun ConstraintViolationException.asValidationDataFetchingGraphQLError(exception: Throwable,
                                                                      path: ExecutionPath,
                                                                      sourceLocation: SourceLocation) =
        ValidationGraphQLError(
                this.constraintViolations.toList().map { it.asConstraintError() },
                path,
                exception,
                sourceLocation
        )

