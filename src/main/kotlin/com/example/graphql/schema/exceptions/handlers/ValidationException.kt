package com.example.graphql.schema.exceptions.handlers

import javax.validation.ConstraintViolation

class ValidationException(val constraintErrors: List<ConstraintError>) : RuntimeException("Validation error")
open class SimpleValidationException(message: String) : RuntimeException(message)

data class ConstraintError(val path: String, val message: String, val type: String)

fun ConstraintViolation<*>.asConstraintError() = ConstraintError(
        path = this.propertyPath.toString(),
        message = this.message,
        type = this.leafBean.toString()
)

