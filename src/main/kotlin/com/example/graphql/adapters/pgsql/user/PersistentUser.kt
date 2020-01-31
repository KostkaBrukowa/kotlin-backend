package com.example.graphql.domain.user

import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.adapters.pgsql.utils.lazyProxy
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.party.PersistentParty
import javax.persistence.*

@Table(name = "users")
@Entity
data class PersistentUser(

        @Id
        @GeneratedValue
        val id: Long? = null,

        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
        val partyRequests: List<PersistentPartyRequest> = emptyList(),

        @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], orphanRemoval = true)
        val ownedParties: List<PersistentParty> = emptyList(),

        @OneToMany(mappedBy = "user")
        val expenses: List<PersistentExpense> = emptyList(),

        @ManyToMany(mappedBy = "users")
        val messageGroups: Set<PersistentMessageGroup> = emptySet(),

        @ManyToMany(mappedBy = "participants")
        @Column(name = "party_id")
        val joinedParties: List<PersistentParty> = emptyList(),

        val name: String?,

        @Column(unique = true)
        val email: String,

        @Column(name = "bank_account")
        val bankAccount: String?,

        val password: String,

        @Column(name = "is_email_confirmed")
        val isEmailConfirmed: Boolean = false
) {
    fun toDomain() = User(
            id = this.id.toString(),
            name = this.name,
            password = this.password,
            bankAccount = this.bankAccount,
            email = this.email,
            joinedParties = lazyProxy(this.joinedParties)?.map {it.toLazyDomain()} ?: emptyList(),
            expenses = emptyList(),
            messageGroups = emptyList(),
            partyRequests = lazyProxy(this.partyRequests)?.map { it.toLazyDomain() } ?: emptyList(),
            isEmailConfirmed = this.isEmailConfirmed
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as PersistentUser

        if (other.id != id) return false
        if (other.name != name) return false
        if (other.email != email) return false
        if (other.bankAccount != bankAccount) return false
        if (other.password != password) return false

        return true
    }

    override fun toString(): String {
        return "PersistentUser"
    }
}

fun User.toPersistentEntity() = PersistentUser(
        id = this.id.toLong(),
        name = this.name,
        bankAccount = this.bankAccount,
        email = this.email,
        password = this.password ?: "",
        expenses = emptyList(),
        messageGroups = emptySet(),
        partyRequests = emptyList(),
        isEmailConfirmed = this.isEmailConfirmed
)

