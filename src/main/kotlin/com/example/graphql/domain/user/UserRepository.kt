package com.example.graphql.domain.user

interface UserRepository {
    fun saveUser(user: User): User
    fun getUserByEmail(email: String): User?
    fun getUserById(id: String): User?
    fun findAllPartyParticipants(partyId: String): List<User>
}
