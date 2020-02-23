package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.payment.*
import org.springframework.stereotype.Component

@Component
class PgSqlBulkPaymentRepository(
        private val bulkPaymentRepository: PersistentBulkPaymentRepository,
        private val userRepository: PersistentUserRepository
) : BulkPaymentRepository {
    override fun findBulkPaymentById(bulkPaymentId: Long): BulkPayment? {
        return bulkPaymentRepository.findById(bulkPaymentId).toNullable()?.toDomainWithRelations()
    }

    override fun findBulkPaymentsByPayerIdOrReceiver(userId: Long): List<BulkPayment> {
        return bulkPaymentRepository.findAllByPayerIdOrReceiverId(userId, userId).map { it.toDomain() }
    }

    override fun updateBulkPaymentStatus(id: Long, status: BulkPaymentStatus) {
        bulkPaymentRepository.updateBulkPaymentStatus(id, status)
    }

    override fun createBulkPayment(amount: Float, payerId: Long, receiverId: Long): BulkPayment {
        val newBulkPayment = PersistentBulkPayment(
                amount = amount,
                payer = userRepository.getOne(payerId),
                receiver = userRepository.getOne(receiverId),
                status = BulkPaymentStatus.IN_PROGRESS
        )

        return bulkPaymentRepository.save(newBulkPayment).toDomain()
    }

    override fun findPaymentsWithPayers(ids: Set<Long>): List<BulkPayment> {
        return bulkPaymentRepository.findPaymentsWithPayers(ids).map {
            it.toDomain().copy(payer = it.payer?.toDomain())
        }
    }

    override fun findPaymentsWithReceivers(ids: Set<Long>): List<BulkPayment> {
        return bulkPaymentRepository.findPaymentsWithReceivers(ids).map {
            it.toDomain().copy(receiver = it.receiver?.toDomain())
        }
    }

    override fun findBulkPaymentsWithPayments(ids: Set<Long>): List<BulkPayment> {
        return bulkPaymentRepository.findPaymentsWithPayments(ids).map {
            it.toDomain().copy(payments = it.payments.map { payment -> payment.toDomain() }.toSet())
        }
    }
}

private fun PersistentBulkPayment.toDomainWithRelations(): BulkPayment =
        this.toDomain().copy(payer = this.payer!!.toDomain(), receiver = this.receiver!!.toDomain())
