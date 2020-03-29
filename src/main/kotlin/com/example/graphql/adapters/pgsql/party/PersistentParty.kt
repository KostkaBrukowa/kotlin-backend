package com.example.graphql.domain.party

import com.example.graphql.adapters.pgsql.message.PersistentPartyMessage
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.user.PersistentUser
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "parties")
@Entity
data class PersistentParty(

        @Id
        @GeneratedValue
        val id: Long = 0,

        val name: String,

        val description: String,

        @Column(name = "start_date")
        val startDate: ZonedDateTime,

        @Column(name = "end_date")
        val endDate: ZonedDateTime?,


        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "owner_id")
        val owner: PersistentUser? = null,

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "party_user", inverseJoinColumns = [JoinColumn(name = "user_id")], joinColumns = [JoinColumn(name = "party_id")])
        val participants: Set<PersistentUser> = emptySet(),

        @OneToMany(mappedBy = "party", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
        val partyRequests: List<PersistentPartyRequest> = emptyList(),

        @OneToMany(mappedBy = "party")
        val expenses: List<PersistentExpense> = emptyList(),

        @OneToMany(mappedBy = "party",fetch = FetchType.LAZY)
        val messages: Set<PersistentPartyMessage> = emptySet()
) {

    fun toDomain(): Party = Party(
            id = this.id,
            name = this.name,
            description = this.description,
            startDate = this.startDate,
            endDate = this.endDate
    )

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistentParty

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false

        return true
    }
}

fun Party.toPersistentEntity() = PersistentParty(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        startDate = this.startDate,
        endDate = this.endDate
)
