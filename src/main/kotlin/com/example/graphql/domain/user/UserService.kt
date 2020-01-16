package com.example.graphql.domain.user

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

//@Component
//class UserService(private val userRepository: UserRepository) {
//    fun findById(id: String): User? {
//        return userRepository.findById(id)
//    }
//}
@Component
class UserService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun getUserById(id: String): User? {
        return userRepository.getUserById(id)
    }
}

