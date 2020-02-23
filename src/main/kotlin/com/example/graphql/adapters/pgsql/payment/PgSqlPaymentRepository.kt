package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.payment.*
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class PgSqlPaymentRepository(
        private val paymentRepository: PersistentPaymentRepository,
        private val bulkPaymentRepository: PersistentBulkPaymentRepository
) : PaymentRepository {
    override fun findPaymentWithOwnerAndExpenseOwner(paymentId: Long): Payment? {
        val payment = paymentRepository.findById(paymentId).toNullable()

        return payment?.toDomainWithRelations()?.copy(
                expense = payment.expense?.toDomain()?.copy(user = payment.expense.user?.toDomain())
        )
    }

    override fun findPaymentsWithOwnerAndExpenseOwner(paymentsIds: List<Long>): List<Payment> {
        return paymentRepository.findAllById(paymentsIds).map {
            it.toDomainWithRelations().copy(
                    expense = it.expense?.toDomain()?.copy(user = it.expense.user?.toDomain())
            )
        }
    }

    override fun findPaymentsWithExpenses(ids: Set<Long>): List<Payment> =
            paymentRepository.findAllById(ids.toList()).map { it.toDomainWithRelations() }

    override fun findPaymentsWithUsers(ids: Set<Long>): List<Payment> =
            paymentRepository.findAllById(ids.toList()).map { it.toDomainWithRelations() }

    override fun getPaymentById(paymentId: Long): Payment? =
            paymentRepository.findById(paymentId).toNullable()?.toDomainWithRelations()

    override fun getPaymentsByUserId(userId: Long) =
            paymentRepository.findAllByUserId(userId).map { it.toDomainWithRelations() }

    override fun createPayments(payments: List<Payment>) {
        paymentRepository.saveAll(payments.map { it.toPersistentEntity() })
    }


    override fun changeExpensePaymentsStatuses(expenseId: Long, status: PaymentStatus) {
        paymentRepository.changeExpensePaymentsStatuses(expenseId, status)
    }

    override fun updatePaymentsStatuses(paymentsIds: List<Long>, status: PaymentStatus) {
        paymentRepository.updatePaymentStatus(paymentsIds, status)
    }

    @Transactional
    override fun updatePaymentsAmounts(updatedPayments: List<Payment>, amount: Float) {
        if (updatedPayments.isEmpty()) return

        paymentRepository.updatePaymentsAmounts(updatedPayments.map { it.id }, amount)
    }

    override fun convertPaymentsToBulkPayment(paymentsIds: List<Long>, bulkPaymentId: Long) {
        paymentRepository.convertPaymentsToBulkPayment(paymentsIds, bulkPaymentId)
    }
}

private fun PersistentPayment.toDomainWithRelations(): Payment =
        this.toDomain().copy(expense = this.expense!!.toDomain(), user = this.user!!.toDomain())
