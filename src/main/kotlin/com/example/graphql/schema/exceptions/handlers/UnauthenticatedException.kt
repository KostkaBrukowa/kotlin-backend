package com.example.graphql.schema.exceptions.handlers

import java.lang.RuntimeException

class UnauthenticatedException : RuntimeException("Token authentication failed")
