package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.party.Party
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.domain.user.toPersistentEntity
import org.hibernate.Hibernate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PgSqlUserRepository(private val userRepository: PersistentUserRepository) : UserRepository {

    override fun saveUser(user: User): User = userRepository.save(user.toPersistentEntity()).toDomain()

    override fun getUserByEmail(email: String): User? = userRepository.findTopByEmail(email)?.toDomain()

    override fun getUserById(id: String): User? = userRepository.findByIdOrNull(id.toLong())?.toDomain()

    override fun findAllPartyParticipants(partyId: String): List<User> =
            userRepository.findAllPartyParticipants(partyId.toLong()).map { it.toDomain() }

    override fun findUsersWithPartyRequests(usersIds: Set<String>): List<User> {
        return userRepository.findUsersWithPartyRequests(usersIds.map { it.toLong() }).map { it.toDomain() }
    }

    override fun findUsersById(usersIds: List<String>): List<User> {
        return userRepository.findAllById(usersIds.map { it.toLong() }).map { it.toDomain() }
    }
}
