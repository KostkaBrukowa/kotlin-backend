package com.example.graphql.domain.expense

import com.example.graphql.adapters.pgsql.message.PersistentExpenseMessage
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.party.toPersistentEntity
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.toPersistentEntity
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "expenses")
@Entity
data class PersistentExpense(

        @Id
        @GeneratedValue
        val id: Long = 0,

        val amount: Float,

        @Column(name = "expense_date")
        val expenseDate: ZonedDateTime,

        val description: String,

        @Enumerated(EnumType.STRING)
        val expenseStatus: ExpenseStatus,


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser?,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "party_id", nullable = true)
        val party: PersistentParty?,

        @OneToMany(mappedBy = "expense", cascade = [CascadeType.REMOVE])
        val payments: List<PersistentPayment> = emptyList(),

        @OneToMany(mappedBy = "expense",fetch = FetchType.LAZY)
        val messages: Set<PersistentExpenseMessage> = emptySet()
) {

    fun toDomain() = Expense(
            id = this.id,
            amount = this.amount,
            expenseDate = this.expenseDate,
            description = this.description,
            expenseStatus = this.expenseStatus
    )

    override fun hashCode(): Int {
        var result = id.hashCode()

        result = 31 * result + amount.hashCode()
        result = 31 * result + expenseDate.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + expenseStatus.hashCode()

        return result
    }
}

fun Expense.toPersistentEntity() = PersistentExpense(
        id = this.id,
        amount = this.amount,
        expenseDate = this.expenseDate,
        description = this.description,
        expenseStatus = this.expenseStatus,
        user = this.user?.toPersistentEntity(),
        party = this.party?.toPersistentEntity()
)
