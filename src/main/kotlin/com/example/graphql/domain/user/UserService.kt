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
    fun findById(id: String): PersistentUser {
        return PersistentUser(
                partyRequests = emptyList(),
                messageGroups = emptyList(),
                password = "dkfsA,",
                name = "fjadks",
                expenses = emptyList(),
                email = "dfka",
                bankAccount = null
        )
    }

    fun saveNewUser(email: String, password: String): Long {
        val encodedPassword = passwordEncoder.encode(password)
        val user = User(email = email, password = encodedPassword)

        return userRepository.saveUser(user) ?: 0
    }

    fun validateUser(email: String, password: String): String? {
        val user = userRepository.getUserByEmail(email) ?: return null
        val passwordMatches = passwordEncoder.matches(password, user.password)

        return if(passwordMatches) user.id else null
    }
}

