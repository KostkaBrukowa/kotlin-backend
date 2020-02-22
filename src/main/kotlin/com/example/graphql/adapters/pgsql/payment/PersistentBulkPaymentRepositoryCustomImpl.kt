package com.example.graphql.adapters.pgsql.payment

import org.springframework.stereotype.Repository
import javax.persistence.EntityManager

@Repository
class PersistentBulkPaymentRepositoryCustomImpl(
        private val em: EntityManager
) : PersistentBulkPaymentRepositoryCustom {

    override fun upsert(payerId: Long, receiverId: Long): Int {
        val sql = """
        INSERT INTO bulk_payments (
            payer_id, receiver_id        
        )
        VALUES (
            :payerId, :receiver_id 
        )
        ON CONFLICT (payer_id, receiver_id) DO NOTHING
        """.trimIndent()

        val updated = em.createNativeQuery(sql).apply {
            setParameter("payerId", payerId)
            setParameter("receiverId", receiverId)
        }.executeUpdate()

        em.clear()

        return updated
    }

    override fun addPaymentToBulkPayment(paymentId: Long, paymentOwnerId: Long, expenseOwnerId: Long): Int {
        val sql = """
        INSERT INTO bulk_payments_current_payments (
             persistent_bulk_payment_id, current_payments_id
        )
        VALUES (
            (SELECT id FROM bulk_payments WHERE payer_id = :paymentOwnerId AND receiver_id = :expenseOwnerId),
            :paymentId
        )
        """.trimIndent()

        val updated = em.createNativeQuery(sql).apply {
            setParameter("paymentId", paymentId)
            setParameter("paymentOwnerId", paymentOwnerId)
            setParameter("expenseOwnerId", expenseOwnerId)
        }.executeUpdate()

        em.clear()

        return updated
    }
}
