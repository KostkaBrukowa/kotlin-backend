package com.example.graphql.domain.expense

import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "expenses")
@Entity
data class PersistentExpense(
        @Id
        @GeneratedValue
        val id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser,

        @OneToMany(mappedBy = "expense")
        val payments: List<PersistentPayment>,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "party_id", nullable = false)
        val party: PersistentParty,

        val amount: String,

        @Column(name= "expense_date")
        val expenseDate: ZonedDateTime,

        val description: String
)
