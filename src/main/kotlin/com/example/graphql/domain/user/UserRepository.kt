package com.example.graphql.domain.user

interface UserRepository {
    fun saveUser(user: User): Long?
    fun getUserByEmail(email: String): User?
}
