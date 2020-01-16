package com.example.graphql.domain.user

import org.springframework.stereotype.Component

@Component
class UserService(private val userRepository: UserRepository) {

    fun getUserById(id: String): User? {
        return userRepository.getUserById(id)
    }
}

