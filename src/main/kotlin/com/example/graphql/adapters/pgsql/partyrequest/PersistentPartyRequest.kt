package com.example.graphql.adapters.pgsql.partyrequest

import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.partyrequest.PartyRequest
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.example.graphql.domain.user.PersistentUser
import javax.persistence.*

@Table(name="party_requests")
@Entity
data class PersistentPartyRequest(

        @Id
        @GeneratedValue
        val id: Long = 0,

        @Enumerated(EnumType.STRING)
        val status: PartyRequestStatus,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "party_id", nullable = false)
        val party: PersistentParty
){

        fun toDomain(): PartyRequest = PartyRequest(
                id = this.id,
                user = null,
                party = null,
                status = this.status
        )
}
