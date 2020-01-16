package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.user.PersistentUser
import org.springframework.data.jpa.repository.JpaRepository

interface PersistentUserRepository: JpaRepository<PersistentUser, Long> {
    fun findTopByEmail(email: String) : PersistentUser?
}
