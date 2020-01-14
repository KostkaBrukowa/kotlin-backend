package com.example.graphql.domain.user

interface UserRepository {
    fun saveUser(user: User, password: String)
}
