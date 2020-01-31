package com.example.graphql.domain.user

import com.example.graphql.domain.party.Party

interface UserRepository {
    fun saveUser(user: User): User

    fun getUserByEmail(email: String): User?

    fun getUserById(id: String): User?

    fun findAllPartyParticipants(partyId: String): List<User>

    fun findUsersWithPartyRequests(usersIds: Set<String>): List<User>

    fun findUsersById(usersIds: List<String>): List<User>
}
