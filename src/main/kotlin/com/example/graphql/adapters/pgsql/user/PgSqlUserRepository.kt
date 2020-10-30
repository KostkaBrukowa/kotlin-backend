package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.user.CannotAddYourselfAsFriendException
import com.example.graphql.domain.user.FriendshipAlreadyExistsException
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@Component
class PgSqlUserRepository(private val userRepository: PersistentUserRepository) : UserRepository {

    override fun saveUser(user: User): User = userRepository.save(user.toPersistentEntity()).toDomain()

    @Throws(EntityNotFoundException::class)
    override fun updateUser(userId: Long, name: String?, bankAccount: String?): User {
        val user = findUserById(userId) ?: throw EntityNotFoundException()
        val newName =name ?: user.name
        val newBankAccount =bankAccount ?: user.bankAccount

        userRepository.updateUser(userId, newName , newBankAccount)

        return user.copy(name = newName, bankAccount = newBankAccount)
    }

    override fun findUserByEmail(email: String): User? = userRepository.findByEmail(email)?.toDomain()

    override fun findUserById(id: Long): User? = userRepository.findByIdOrNull(id)?.toDomain()

    override fun findAllPartyParticipants(partyId: Long): List<User> =
            userRepository.findAllPartyParticipants(partyId).map { it.toDomain() }

    override fun findUsersFriends(userId: Long): List<User> {
        val user = userRepository.findUsersFriends(userId)
        val uniqueFriends = user.friends + user.friendOf

        return uniqueFriends.map { it.toDomain() }
    }

    override fun findUsersWithPartyRequests(usersIds: Set<Long>): List<User> {
        return userRepository.findUsersWithPartyRequests(usersIds).map {
            it.toDomain()
        }
    }

    override fun findUsersWithExpenses(usersIds: Set<Long>): List<User> {
        return userRepository.findUsersWithExpenses(usersIds).map {
            it.toDomain().copy(expenses = it.expenses.map { expense -> expense.toDomain() })
        }
    }

    override fun findUsersWithPayments(usersIds: Set<Long>): List<User> {
        val payments = userRepository.findUsersWithPayments(usersIds)

        return payments.map {
            it.toDomain().copy(payments = it.payments.map { payment -> payment.toDomain() })
        }
    }

    override fun findUsersWithJoinedParties(usersIds: Set<Long>): List<User> {
        val parties = userRepository.findUsersWithJoinedParties(usersIds)

        return parties.map {
            it.toDomain().copy(joinedParties = it.joinedParties.map { party -> party.toDomain() })
        }
    }

    @Throws(EntityNotFoundException::class)
    @Transactional
    override fun addFriend(userId: Long, friendEmail: String): User {
        val friend = userRepository.findUserByEmailWithFriends(friendEmail)
                ?: throw EntityNotFoundException("Użytkownik o takim mailu nie istnieje")

        if (friend.friends.any { it.id == userId } || friend.friendOf.any { it.id == userId }) {
            throw FriendshipAlreadyExistsException()
        }

        if (friend.id == userId) {
            throw CannotAddYourselfAsFriendException()
        }

        userRepository.addFriend(userId, friend.id)

        return friend.toDomain()
    }

    override fun removeFriend(userId: Long, friendId: Long) {
        userRepository.removeFriend(userId, friendId)
    }

    override fun findUsersById(usersIds: List<Long>): List<User> {
        return userRepository.findAllById(usersIds).map { it.toDomain() }
    }
}
