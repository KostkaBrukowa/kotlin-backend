package com.example.graphql.adapters.pgsql.message

import org.springframework.data.jpa.repository.JpaRepository

interface PersistentBulkPaymentMessageRepository : JpaRepository<PersistentBulkPaymentMessage, Long>

interface PersistentExpenseMessageRepository : JpaRepository<PersistentExpenseMessage, Long>

interface PersistentPartyMessageRepository : JpaRepository<PersistentPartyMessage, Long>

interface PersistentPaymentMessageRepository : JpaRepository<PersistentPaymentMessage, Long>
