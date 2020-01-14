package com.example.graphql.domain.partyrequest

import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.user.PersistentUser
import com.expediagroup.graphql.annotations.GraphQLID
import javax.persistence.*

@Table(name="party_requests")
@Entity
data class PersistentPartyRequest(
        @Id
        @GeneratedValue
        val id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "party_id", nullable = false)
        val party: PersistentParty,

        @Enumerated(EnumType.STRING)
        val status: PartyRequestStatus
)

//enum class PartyRequestStatus {
//    ACCEPTED,
//    DECLINED,
//    IN_PROGRESS,
//}
