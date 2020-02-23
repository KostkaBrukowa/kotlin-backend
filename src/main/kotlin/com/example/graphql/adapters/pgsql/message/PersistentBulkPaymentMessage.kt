package com.example.graphql.adapters.pgsql.message

import com.example.graphql.adapters.pgsql.payment.PersistentBulkPayment
import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.domain.user.PersistentUser
import javax.persistence.*


@Table(name = "bulk_payment_messages")
@Entity
class PersistentBulkPaymentMessage(

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "bulk_payment_id", nullable = false)
        val bulkPayment: PersistentBulkPayment? = null
) : PersistentMessage()
