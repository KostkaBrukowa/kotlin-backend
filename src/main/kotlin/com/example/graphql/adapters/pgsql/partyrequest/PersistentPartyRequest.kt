package com.example.graphql.adapters.pgsql.partyrequest

import com.example.graphql.adapters.pgsql.utils.lazyProxy
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

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "party_id", nullable = false)
        val party: PersistentParty?,

        @Enumerated(EnumType.STRING)
        val status: PartyRequestStatus
){
        fun toDomain(): PartyRequest = PartyRequest(
                id = this.id.toString(),
                user = lazyProxy(this.user)?.toDomain(),
                party = this.party?.toDomain(),
                status = this.status
        )

        fun toLazyDomain(): PartyRequest = PartyRequest(
                id = this.id.toString(),
                user = null,
                party = null,
                status = this.status
        )
}
