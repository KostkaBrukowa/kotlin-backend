package com.example.graphql.domain.party

import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.user.PersistentUser
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "parties")
@Entity
data class PersistentParty(

        @Id
        @GeneratedValue
        val id: Long = 0,

        var name: String,

        var description: String,

        @Column(name = "start_date")
        var startDate: ZonedDateTime,

        @Column(name = "end_date")
        var endDate: ZonedDateTime?,


        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "owner_id")
        val owner: PersistentUser? = null,

        @OneToOne(fetch = FetchType.LAZY, optional = true) // TODO CHANGE OPTIONAL TO FALSE WHEN MESSAGE GROUP IS READY
        @JoinColumn(name = "messagegroup_id")
        val messageGroup: PersistentMessageGroup? = null,

        @ManyToMany(
                fetch = FetchType.LAZY,
                cascade = [CascadeType.MERGE]
        )
        @JoinTable(name = "party_user", inverseJoinColumns = [JoinColumn(name = "user_id")], joinColumns = [JoinColumn(name = "party_id")])
        val participants: Set<PersistentUser> = emptySet(),

        @OneToMany(mappedBy = "party", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
        val partyRequests: List<PersistentPartyRequest> = emptyList(),

        @OneToMany(mappedBy = "party")
        val expenses: List<PersistentExpense> = emptyList()
) {

    fun toDomain(): Party = Party(
            id = this.id,
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate
    )

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

fun Party.toPersistentEntity() = PersistentParty(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        startDate = this.startDate,
        endDate = this.endDate
)
