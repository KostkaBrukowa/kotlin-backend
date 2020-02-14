package com.example.graphql.adapters.pgsql.payment

import com.example.graphql.adapters.pgsql.utils.toNullable
import com.example.graphql.domain.payment.*
import org.springframework.stereotype.Component

@Component
class PgSqlPaymentRepository(private val paymentRepository: PersistentPaymentRepository) : PaymentRepository {
    override fun findPaymentWithOwnerAndExpenseOwner(paymentId: Long): Payment? {
        val payment = paymentRepository.findById(paymentId).toNullable()

        return payment?.toDomainWithRelations()?.copy(
                expense = payment.expense?.toDomain()?.copy(user = payment.expense.user?.toDomain())
        )
    }

    override fun findPaymentsWithExpenses(ids: Set<Long>): List<Payment> =
            paymentRepository.findAllById(ids.toList()).map { it.toDomainWithRelations() }

    override fun findPaymentsWithUsers(ids: Set<Long>): List<Payment> =
            paymentRepository.findAllById(ids.toList()).map { it.toDomainWithRelations() }


    override fun getPaymentById(paymentId: Long): Payment? {
        return paymentRepository.findById(paymentId).toNullable()?.toDomainWithRelations()
    }

    override fun getPaymentsByUserId(userId: Long): List<Payment> {
        return paymentRepository.findAllByUserId(userId).map { it.toDomainWithRelations() }
    }

    override fun createPayments(payments: List<Payment>) {
        paymentRepository.saveAll(payments.map { it.toPersistentEntity() })
    }

    override fun changeExpensePaymentsStatuses(expenseId: Long, status: PaymentStatus) {
        paymentRepository.changeExpensePaymentsStatuses(expenseId, status)
    }

    override fun updatePaymentStatus(paymentId: Long, status: PaymentStatus) {
        paymentRepository.updatePaymentStatus(paymentId, status)
    }
}

private fun PersistentPayment.toDomainWithRelations(): Payment =
        this.toDomain().copy(expense = this.expense!!.toDomain(), user = this.user!!.toDomain())
