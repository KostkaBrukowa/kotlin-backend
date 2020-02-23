package com.example.graphql.domain.user

import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.adapters.pgsql.utils.lazyProxy
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.payment.PersistentPayment
import javax.persistence.*

@Table(name = "users")
@Entity
data class PersistentUser(

        @Id
        @GeneratedValue
        val id: Long = 0,

        val name: String?,

        @Column(unique = true)
        val email: String,

        @Column(name = "bank_account")
        val bankAccount: String?,

        val password: String,

        @Column(name = "is_email_confirmed")
        val isEmailConfirmed: Boolean = false,


        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
        val partyRequests: List<PersistentPartyRequest> = emptyList(),

        @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], orphanRemoval = true)
        val ownedParties: List<PersistentParty> = emptyList(),

        @OneToMany(mappedBy = "user")
        val expenses: Set<PersistentExpense> = emptySet(),

        @OneToMany(mappedBy = "user")
        val payments: Set<PersistentPayment> = emptySet(),

        @OneToMany(mappedBy = "user")
        val messages: Set<PersistentMessage> = emptySet(),

        @ManyToMany(mappedBy = "participants")
        @Column(name = "party_id")
        val joinedParties: Set<PersistentParty> = emptySet(),

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "friends",
                joinColumns = [JoinColumn(name = "userId")],
                inverseJoinColumns = [JoinColumn(name = "friendId")]
        )
        val friends: Set<PersistentUser> = emptySet(),

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "friends",
                joinColumns = [JoinColumn(name = "friendId")],
                inverseJoinColumns = [JoinColumn(name = "userId")]
        )
        val friendOf: Set<PersistentUser> = emptySet()
) {

    fun toDomain() = User(
            id = this.id,
            name = this.name,
            password = this.password,
            bankAccount = this.bankAccount,
            email = this.email,
            joinedParties = lazyProxy(this.joinedParties)?.map { it.toDomain() } ?: emptyList(),
            expenses = emptyList(),
            messageGroups = emptyList(),
            partyRequests = lazyProxy(this.partyRequests)?.map { it.toDomain() } ?: emptyList(),
            isEmailConfirmed = this.isEmailConfirmed
    )

    override fun hashCode(): Int {
        var result = id.hashCode()

        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + bankAccount.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + isEmailConfirmed.hashCode()

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as PersistentUser

        return this.id == other.id
    }
}

fun User.toPersistentEntity() = PersistentUser(
        id = this.id,
        name = this.name,
        bankAccount = this.bankAccount,
        email = this.email,
        password = this.password,
        isEmailConfirmed = this.isEmailConfirmed
)

