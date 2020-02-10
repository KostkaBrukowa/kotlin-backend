package com.example.graphql.domain.user

import org.springframework.stereotype.Component

@Component
class UserService(
        private val userRepository: UserRepository
) {

    fun getUserById(id: Long): User? = userRepository.getUserById(id)
}

