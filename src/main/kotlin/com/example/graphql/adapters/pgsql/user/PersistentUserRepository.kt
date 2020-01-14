package com.example.graphql.adapters.pgsql.user

import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.UserRepository
import org.springframework.data.jpa.repository.JpaRepository

interface PersistentUserRepository: JpaRepository<PersistentUser, Long> {}
