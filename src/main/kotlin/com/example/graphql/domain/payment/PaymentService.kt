package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.PaymentStatusNotValid
import com.example.graphql.domain.expense.requireExpenseOwner
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.payment.UpdatePaymentStatusInput
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component
import javax.persistence.EntityNotFoundException

@Component
class PaymentService(private val paymentRepository: PaymentRepository) {

    // GET
    fun getUserPayments(userId: Long): List<Payment> {
        return paymentRepository.getPaymentsByUserId(userId)
    }

    fun getPaymentById(paymentId: Long): Payment? {
        return paymentRepository.getPaymentById(paymentId)
    }


    // CREATE
    fun createPaymentsForExpense(newExpense: Expense, participants: Set<Long>) {
        val newPayments = participants.map {
            Payment(
                    amount = null,
                    confirmImageUrl = null,
                    status = PaymentStatus.IN_PROGRESS,
                    expense = newExpense,
                    user = User(it)
            )
        }

        paymentRepository.createPayments(newPayments)
    }


    // UPDATE
    fun updatePaymentStatus(updatePaymentStatusInput: UpdatePaymentStatusInput, currentUserId: Long): Payment {
        val payment = paymentRepository.findPaymentWithOwnerAndExpenseOwner(updatePaymentStatusInput.paymentId)
                ?: throw EntityNotFoundException("payment")

        if (updatePaymentStatusInput.status == PaymentStatus.CONFIRMED) {
            requireExpenseOwner(payment.expense, currentUserId)
        } else {
            requirePaymentOwner(payment, currentUserId)
        }

        requireCorrectPaymentStatus(payment.status, updatePaymentStatusInput.status)

        val updatedPayment = payment.copy(status = updatePaymentStatusInput.status)

        paymentRepository.updatePaymentStatus(payment.id, updatePaymentStatusInput.status)

        return updatedPayment
    }

    private fun requirePaymentOwner(payment: Payment, currentUserId: Long) {
        if (payment.user == null) throw InternalError("Payment was not entirely fetched")
        if (payment.user.id != currentUserId) throw UnauthorisedException()
    }

    private fun requireCorrectPaymentStatus(statusFrom: PaymentStatus, statusTo: PaymentStatus) {
        when (statusFrom) {
            PaymentStatus.IN_PROGRESS -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.ACCEPTED, PaymentStatus.DECLINED))
            }
            PaymentStatus.PAID -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.DECLINED, PaymentStatus.CONFIRMED))
            }
            PaymentStatus.ACCEPTED -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.DECLINED, PaymentStatus.PAID))
            }
            PaymentStatus.DECLINED -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.ACCEPTED))
            }
            PaymentStatus.CONFIRMED -> throw PaymentStatusNotValid(PaymentStatus.CONFIRMED)
        }
    }

    private fun requirePaymentStatuses(statusTo: PaymentStatus, availableStatuses: List<PaymentStatus>) {
        if (!availableStatuses.contains(statusTo)) throw PaymentStatusNotValid(statusTo)
    }

    fun resetPaymentsStatuses(expenseId: Long) {
        paymentRepository.changeExpensePaymentsStatuses(expenseId, PaymentStatus.IN_PROGRESS)
    }

    // DELETE
}
