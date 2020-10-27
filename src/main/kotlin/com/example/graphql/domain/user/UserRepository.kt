package com.example.graphql.domain.user

interface UserRepository {
    fun saveUser(user: User): User
    fun updateUser(userId: Long, name: String?, bankAccount: String?): User

    fun findUserByEmail(email: String): User?
    fun findUserById(id: Long): User?
    fun findUsersById(usersIds: List<Long>): List<User>
    fun findAllPartyParticipants(partyId: Long): List<User>
    fun findUsersFriends(userId: Long): List<User>

    fun findUsersWithPartyRequests(usersIds: Set<Long>): List<User>
    fun findUsersWithExpenses(usersIds: Set<Long>): List<User>
    fun findUsersWithPayments(usersIds: Set<Long>): List<User>
    fun findUsersWithJoinedParties(usersIds: Set<Long>): List<User>


    fun addFriend(userId: Long, friendEmail: String): User
    fun removeFriend(userId: Long, friendId: Long)
}
