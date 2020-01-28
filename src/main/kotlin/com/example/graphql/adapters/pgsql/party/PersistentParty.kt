package com.example.graphql.domain.party

import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.partyrequest.PersistentPartyRequest
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

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id")
        val owner: PersistentUser?,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "party_user", inverseJoinColumns = [JoinColumn(name = "user_id")], joinColumns = [JoinColumn(name = "party_id")])
        val participants: List<PersistentUser>,

        @OneToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "messagegroup_id")
        val messageGroup: PersistentMessageGroup?,

        @OneToMany(mappedBy = "party", fetch = FetchType.EAGER)
        val partyRequests: Set<PersistentPartyRequest>,

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
            owner = this.owner?.toDomain(),
            participants = this.participants.map { it.toDomain() },
            messageGroup = null,
            partyRequests = emptyList(),
            expenses = emptyList(),
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate
    )
}

fun Party.toPersistentEntity() = PersistentParty(
        id = this.id.toLong(),
        owner = this.owner?.toPersistentEntity(),
        participants = this.participants.map { it.toPersistentEntity() },
        messageGroup = null,
        partyRequests = emptySet(),
        expenses = emptyList(),
        name = this.name,
        description = this.description ?: "",
        startDate = this.startDate,
        endDate = this.endDate
)
