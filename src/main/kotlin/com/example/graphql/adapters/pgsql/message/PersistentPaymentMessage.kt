package com.example.graphql.adapters.pgsql.message

import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.domain.payment.PersistentPayment
import javax.persistence.*

@Table(name = "payment_messages")
@Entity
class PersistentPaymentMessage(

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "payment_id", nullable = false)
        val payment: PersistentPayment? = null
) : PersistentMessage()
