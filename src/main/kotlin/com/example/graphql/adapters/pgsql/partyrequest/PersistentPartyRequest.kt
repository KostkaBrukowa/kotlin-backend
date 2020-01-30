package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.lazyProxy
import javax.persistence.*

@Table(name="party_requests")
@Entity
data class PersistentPartyRequest(

        @Id
        @GeneratedValue
        val id: Long? = null,

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
                id = this.id?.toString() ?: "",
                user = lazyProxy(this.user)?.toDomain(),
                party = this.party?.toDomain(),
                status = this.status
        )

        fun toLazyDomain(): PartyRequest = PartyRequest(
                id = this.id?.toString() ?: "",
                user = null,
                party = null,
                status = this.status
        )
}



//enum class PartyRequestStatus {
//    ACCEPTED,
//    DECLINED,
//    IN_PROGRESS,
//}
