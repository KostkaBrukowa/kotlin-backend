package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.domain.payment.BulkPayment
import com.example.graphql.domain.payment.BulkPaymentStatus
import com.example.graphql.domain.payment.Payment
import com.example.graphql.domain.payment.PersistentPayment
import com.example.graphql.domain.user.PersistentUser
import javax.persistence.*

@Table(name = "bulk_payments", uniqueConstraints = [UniqueConstraint(columnNames = ["payer_id", "receiver_id"])])
@Entity
data class PersistentBulkPayment(

        @Id
        @GeneratedValue
        val id: Long = 0,

        val amount: Float,

        @Column(name = "confirm_image_url")
        val confirmImageUrl: String? = null,

        @Enumerated(EnumType.STRING)
        val status: BulkPaymentStatus,


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "payer_id", nullable = false)
        val payer: PersistentUser? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "receiver_id", nullable = false)
        val receiver: PersistentUser? = null,

        @OneToMany(mappedBy = "bulkedPayment", fetch = FetchType.LAZY)
        val payments: Set<PersistentPayment> = emptySet()
) {

    fun toDomain() = BulkPayment(
            id = this.id,
            amount = this.amount,
            confirmImageUrl = this.confirmImageUrl
    )
}

