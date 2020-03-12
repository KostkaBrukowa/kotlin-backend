package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.adapters.pgsql.expense.toDomainWithRelations
import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.message.Message
import com.example.graphql.domain.payment.*
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class PgSqlPaymentRepository(
        private val paymentRepository: PersistentPaymentRepository
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

    override fun findBulkPaymentsWithMessages(ids: Set<Long>): Map<Payment, List<Message>> {
        return paymentRepository
                .findPaymentsWithMessages(ids)
                .associateBy({ it.toDomain() }, { it.messages.map { message -> message.toDomain() } })
    }

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

    override fun updatePaymentsStatuses(paymentsIds: List<Long>, status: PaymentStatus): List<Payment> {
        paymentRepository.updatePaymentStatus(paymentsIds, status)

        return paymentRepository.findAllById(paymentsIds)
                .map {
                    it.toDomainWithRelations().copy(expense = it.expense?.toDomainWithRelations())
                }
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

fun PersistentPayment.toDomainWithRelations(): Payment =
        this.toDomain().copy(expense = this.expense!!.toDomain(), user = this.user!!.toDomain())
