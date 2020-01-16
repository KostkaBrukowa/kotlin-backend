package com.example.graphql.domain.user

interface UserRepository {
    fun saveUser(user: User): Long?
    fun getUserByEmail(email: String): User?
    fun getUserById(id: String): User?
}
