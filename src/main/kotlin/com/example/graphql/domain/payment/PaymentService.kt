package com.example.graphql.domain.payment

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.PaymentStatusNotValid
import com.example.graphql.domain.expense.requireExpenseOwner
import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.payment.UpdatePaymentStatusInput
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import javax.persistence.EntityNotFoundException
import kotlin.contracts.contract

@Component
class PaymentService(
        private val paymentRepository: PaymentRepository,
        private val notificationService: NotificationService
) {

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
                    user = User(it),
                    createdAt = ZonedDateTime.now()
            )
        }

        paymentRepository.createPayments(newPayments)
        notificationService.newExpensePaymentsNotification(newExpense, newPayments)
    }


    // UPDATE
    fun updatePaymentStatus(updatePaymentStatusInput: UpdatePaymentStatusInput, currentUserId: Long): Payment {
        val payment = paymentRepository.findPaymentWithOwnerAndExpenseOwner(updatePaymentStatusInput.paymentId.toLong())
                ?: throw EntityNotFoundException("payment")

        requireCorrectPaymentStatus(payment.status, updatePaymentStatusInput.status)

        if (updatePaymentStatusInput.status == PaymentStatus.CONFIRMED) {
            requireExpenseOwner(payment.expense, currentUserId)
        } else {
            requirePaymentOwner(payment.user, currentUserId)
        }


        val updatedPayment = payment.copy(status = updatePaymentStatusInput.status)

        updatePaymentsStatuses(listOf(payment.id), updatePaymentStatusInput.status)

        return updatedPayment
    }

    fun updatePaymentsStatuses(paymentsIds: List<Long>, status: PaymentStatus) {
        val updatedPayments = paymentRepository.updatePaymentsStatuses(paymentsIds, status)
        notificationService.updatePaymentsStatusesNotifications(updatedPayments, status)
    }

    fun updatePaymentsAmount(payments: List<Payment>, amount: Float) {
        paymentRepository.updatePaymentsAmounts(payments, amount)
    }


    private fun requireCorrectPaymentStatus(statusFrom: PaymentStatus, statusTo: PaymentStatus) {
        when (statusFrom) {
            PaymentStatus.IN_PROGRESS -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.ACCEPTED, PaymentStatus.DECLINED))
            }
            PaymentStatus.PAID -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.CONFIRMED))
            }
            PaymentStatus.ACCEPTED -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.DECLINED, PaymentStatus.PAID))
            }
            PaymentStatus.DECLINED -> {
                requirePaymentStatuses(statusTo, listOf(PaymentStatus.ACCEPTED))
            }
            PaymentStatus.CONFIRMED, PaymentStatus.BULKED -> throw PaymentStatusNotValid(PaymentStatus.CONFIRMED)
        }
    }

    fun resetPaymentsStatuses(expenseId: Long) {
        paymentRepository.changeExpensePaymentsStatuses(expenseId, PaymentStatus.IN_PROGRESS)
    }

    // DELETE
}

fun requirePaymentStatuses(statusTo: PaymentStatus, availableStatuses: List<PaymentStatus>) {
    if (!availableStatuses.contains(statusTo)) throw PaymentStatusNotValid(statusTo)
}

private fun requirePaymentOwner(owner: User?, currentUserId: Long) {
    contract {
        returns() implies (owner != null)
    }

    if (owner == null) throw InternalError("Payment was not entirely fetched")
    if (owner.id != currentUserId) throw UnauthorisedException()
}


