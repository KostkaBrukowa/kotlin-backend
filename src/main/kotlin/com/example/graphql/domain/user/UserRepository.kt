package com.example.graphql.domain.user

interface UserRepository {
    fun saveUser(user: User): User

    fun getUserByEmail(email: String): User?

    fun getUserById(id: Long): User?

    fun findAllPartyParticipants(partyId: Long): List<User>

    fun findUsersWithPartyRequests(usersIds: Set<Long>): List<User>
    fun findUsersWithExpenses(usersIds: Set<Long>): List<User>
    fun findUsersWithPayments(usersIds: Set<Long>): List<User>
    fun findUsersWithJoinedParties(usersIds: Set<Long>): List<User>

    fun findUsersById(usersIds: List<Long>): List<User>
}
