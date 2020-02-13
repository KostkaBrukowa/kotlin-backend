package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class PgSqlUserRepository(private val userRepository: PersistentUserRepository) : UserRepository {

    override fun saveUser(user: User): User = userRepository.save(user.toPersistentEntity()).toDomain()

    override fun getUserByEmail(email: String): User? = userRepository.findTopByEmail(email)?.toDomain()

    override fun getUserById(id: Long): User? = userRepository.findByIdOrNull(id)?.toDomain()

    override fun findAllPartyParticipants(partyId: Long): List<User> =
            userRepository.findAllPartyParticipants(partyId).map { it.toDomain() }

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

    override fun findUsersById(usersIds: List<Long>): List<User> {
        return userRepository.findAllById(usersIds).map { it.toDomain() }
    }
}
