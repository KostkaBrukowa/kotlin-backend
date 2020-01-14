package com.example.graphql.domain.user

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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

    fun saveNewUser(user: User, password: String) {
        userRepository.saveUser(user, passwordEncoder.encode(password))
    }
}

