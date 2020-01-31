package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.user.PersistentUser
import javax.persistence.*

@Table(name="payments")
@Entity
data class PersistentPayment(
        @Id
        @GeneratedValue
        val id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "expense_id", nullable = false)
        val expense: PersistentExpense,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser,

        val amount: String?,

        @Column(name = "confirm_image_url")
        val confirmImageUrl: String?,

        @Enumerated(EnumType.STRING)
        @Column(name = "payment_status")
        val payment_status: PaymentStatus
)

//enum class PaymentStatus {
//    ACCEPTED,
//    DECLINED,
//    IN_PROGRESS,
//    PAID,
//}
//
