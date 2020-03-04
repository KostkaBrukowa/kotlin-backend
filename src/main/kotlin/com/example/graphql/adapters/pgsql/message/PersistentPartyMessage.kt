package com.example.graphql.adapters.pgsql.message

import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.domain.party.PersistentParty
import javax.persistence.*

@Table(name = "party_messages")
@Entity
class PersistentPartyMessage(

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "party_id", nullable = false)
        val party: PersistentParty? = null
) : PersistentMessage()
