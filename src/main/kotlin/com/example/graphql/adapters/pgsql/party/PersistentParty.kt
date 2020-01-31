package com.example.graphql.domain.party

import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.adapters.pgsql.utils.lazyProxy
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.toPersistentEntity
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "parties")
@Entity
data class PersistentParty(

        @Id
        @GeneratedValue
        val id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "owner_id")
        val owner: PersistentUser?,

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "party_user", inverseJoinColumns = [JoinColumn(name = "user_id")], joinColumns = [JoinColumn(name = "party_id")])
        val participants: Set<PersistentUser>,

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "messagegroup_id")
        val messageGroup: PersistentMessageGroup?,

        @OneToMany(mappedBy = "party", fetch = FetchType.LAZY)
        val partyRequests: List<PersistentPartyRequest>? = null,

        @OneToMany(mappedBy = "party")
        val expenses: List<PersistentExpense>,

        var name: String,

        var description: String,

        @Column(name = "start_date")
        var startDate: ZonedDateTime,

        @Column(name = "end_date")
        var endDate: ZonedDateTime?
) {

    fun toDomain(): Party = Party(
            id = this.id.toString(),
            owner = lazyProxy(this.owner)?.toDomain(),
            participants = lazyProxy(this.participants)?.map { it.toDomain() } ?: emptyList(),
            messageGroup = null,
            expenses = emptyList(),
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate
    )
    fun toLazyDomain(): Party = Party(
            id = this.id.toString(),
            owner = null,
            participants = emptyList(),
            messageGroup = null,
            expenses = emptyList(),
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as PersistentParty

        if (other.id != id) return false
        if (other.name != name) return false
        if (other.description != description) return false
        if (other.startDate != startDate) return false
        if (other.endDate != endDate) return false

        return true
    }
}

fun Party.toPersistentEntity() = PersistentParty(
        id = this.id.toLong(),
        owner = this.owner?.toPersistentEntity(),
        participants = this.participants.map { it.toPersistentEntity() }.toSet(),
        messageGroup = null,
        expenses = emptyList(),
        name = this.name,
        description = this.description ?: "",
        startDate = this.startDate,
        endDate = this.endDate
)
