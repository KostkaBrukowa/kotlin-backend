package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.User
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.stereotype.Component

@Component
class PgSqlUserRepository(private val userRepository: PersistentUserRepository) : UserRepository {

    override fun saveUser(user: User): Long? = userRepository.save(user.toPersistentEntity()).id

    override fun getUserByEmail(email: String): User? = userRepository.findTopByEmail(email)?.toDomain()

    override fun getUserById(id: String): User? = userRepository.getOne(id.toLong()).toDomain()
}
