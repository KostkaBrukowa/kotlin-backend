package com.example.graphql.schema.exceptions.handlers

class UnauthenticatedException : RuntimeException("Token authentication failed")
class UnauthorisedException : RuntimeException("Użytkownik nie ma uprawnień do wykonania tej akcji")
class EntityNotFoundException(entityName: String) : RuntimeException("Encja $entityName nie została znaleziona")
class InternalServerError : RuntimeException("InternalServerError")
class InvalidActionException(errorMessage: String) : RuntimeException(errorMessage)
