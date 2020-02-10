package com.example.graphql.schema.exceptions.handlers

class UnauthenticatedException : RuntimeException("Token authentication failed")
class UnauthorisedException : RuntimeException("User is not authorised to perform this action")
class EntityNotFoundException(entityName: String) : Exception("Entity $entityName was not found")
class InternalServerError : RuntimeException("InternalServerError")
class InvalidActionException(errorMessage: String) : RuntimeException(errorMessage)
