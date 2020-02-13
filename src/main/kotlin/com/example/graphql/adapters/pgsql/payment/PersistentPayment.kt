package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.user.PersistentUser
import javax.persistence.*

@Table(name = "payments")
@Entity
data class PersistentPayment(
        @Id
        @GeneratedValue
        val id: Long = 0,

        val amount: Float?,

        @Column(name = "confirm_image_url")
        val confirmImageUrl: String?,

        @Enumerated(EnumType.STRING)
        @Column(name = "payment_status")
        val paymentStatus: PaymentStatus,


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "expense_id", nullable = false)
        val expense: PersistentExpense? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser? = null
) {
    fun toDomain() = Payment(
            id = this.id,
            amount = this.amount,
            confirmImageUrl = this.confirmImageUrl,
            status = this.paymentStatus
    )
}

