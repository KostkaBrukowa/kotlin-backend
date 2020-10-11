package com.example.graphql.domain.user

import com.example.graphql.schema.exceptions.handlers.SimpleValidationException
import org.springframework.stereotype.Component

@Component
class UserService(
        private val userRepository: UserRepository
) {

    fun getUserById(id: Long): User? = userRepository.findUserById(id)

    fun findUsersFriends(userId: Long) = userRepository.findUsersFriends(userId)

    fun addFriend(currentUserId: Long, friendEmail: String): User {
        return userRepository.addFriend(currentUserId, friendEmail)
    }

    fun removeFriend(userId: Long, currentUserId: Long) {
        userRepository.removeFriend(userId, currentUserId)
    }
}

class FriendshipAlreadyExistsException : SimpleValidationException("Friendship already exists")

