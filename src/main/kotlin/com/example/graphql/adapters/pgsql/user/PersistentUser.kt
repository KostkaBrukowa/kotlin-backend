package com.example.graphql.domain.user

import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.partyrequest.PersistentPartyRequest
import javax.persistence.*

@Table(name="users")
@Entity
data class PersistentUser(
        @Id
        @GeneratedValue
        val id: Long? = null,

        @OneToMany(mappedBy = "user")
        val partyRequests: List<PersistentPartyRequest> = emptyList(),

        @OneToMany(mappedBy = "user")
        val expenses: List<PersistentExpense> = emptyList(),

        @ManyToMany(mappedBy = "users")
        val messageGroups: List<PersistentMessageGroup> = emptyList(),

        val name: String?,

        @Column(unique = true)
        val email: String,

        @Column(name ="bank_account")
        val bankAccount: String?,

        val password: String,

        @Column(name ="is_email_confirmed")
        val isEmailConfirmed: Boolean = false
) {

        fun toDomain() = User(
                id = this.id.toString(),
                name = this.name,
                password = this.password,
                bankAccount = this.bankAccount,
                email = this.email,
                expenses = emptyList(),
                messageGroups = emptyList(),
                partyRequests = emptyList(),
                isEmailConfirmed = this.isEmailConfirmed
        )
}

fun User.toPersistentEntity() = PersistentUser(
        id = this.id.toLong(),
        name = this.name,
        bankAccount = this.bankAccount,
        email = this.email,
        password = this.password ?: "",
        expenses = emptyList(),
        messageGroups = emptyList(),
        partyRequests = emptyList(),
        isEmailConfirmed = this.isEmailConfirmed
)
