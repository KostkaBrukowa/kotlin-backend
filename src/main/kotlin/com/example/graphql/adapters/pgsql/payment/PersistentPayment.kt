package com.example.graphql.domain.payment

import com.example.graphql.adapters.pgsql.message.PersistentPaymentMessage
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPayment
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.expense.toPersistentEntity
import com.example.graphql.domain.user.PersistentUser
import com.example.graphql.domain.user.toPersistentEntity
import org.springframework.data.annotation.CreatedDate
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "payments")
@Entity
data class PersistentPayment(
        @Id
        @GeneratedValue
        val id: Long = 0,

        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        val createdAt: ZonedDateTime,

        @Column(name = "paid_at", nullable = true)
        val paidAt: ZonedDateTime?,

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
        val user: PersistentUser? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "bulked_payment_id", nullable = true)
        val bulkedPayment: PersistentBulkPayment? = null,

        @OneToMany(mappedBy = "payment",fetch = FetchType.LAZY)
        val messages: Set<PersistentPaymentMessage> = emptySet()
) {
    fun toDomain() = Payment(
            id = this.id,
            amount = this.amount,
            confirmImageUrl = this.confirmImageUrl,
            status = this.paymentStatus,
            createdAt = this.createdAt,
            paidAt = this.paidAt
    )

    override fun hashCode(): Int {
        var result = id.hashCode()

        result = 31 * result + paymentStatus.hashCode()

        return result
    }
}

fun Payment.toPersistentEntity() = PersistentPayment(
        id = this.id,
        amount = this.amount,
        confirmImageUrl = this.confirmImageUrl,
        paymentStatus = this.status,
        expense = this.expense?.toPersistentEntity(),
        user = this.user?.toPersistentEntity(),
        createdAt = this.createdAt,
        paidAt = this.paidAt
)
