package com.example.graphql.domain.payment

import com.example.graphql.domain.user.User
import com.example.graphql.resolvers.payment.UpdateBulkPaymentStatusInput
import com.example.graphql.schema.exceptions.handlers.EntityNotFoundException
import com.example.graphql.schema.exceptions.handlers.SimpleValidationException
import com.example.graphql.schema.exceptions.handlers.UnauthorisedException
import org.springframework.stereotype.Component

@Component
class BulkPaymentService(
        private val bulkPaymentRepository: BulkPaymentRepository,
        private val paymentRepository: PaymentRepository
) {

    fun getUserBulkPayments(userId: Long): List<BulkPayment> {
        return bulkPaymentRepository.findBulkPaymentsByPayerIdOrReceiver(userId)
    }

    fun convertPaymentsToBulkPayment(paymentsIds: List<Long>, currentUserId: Long): BulkPayment? {
        if (paymentsIds.isEmpty()) return null
        val payments = paymentRepository.findPaymentsWithOwnerAndExpenseOwner(paymentsIds)

        if (payments.size != paymentsIds.size) throw EntityNotFoundException("payment")

        payments.forEach {
            requirePaymentStatuses(it.status, listOf(
                    PaymentStatus.ACCEPTED,
                    PaymentStatus.IN_PROGRESS,
                    PaymentStatus.DECLINED
            ))
        }
        val (payerId, receiverId, bulkPaymentAmount) = calculateBulkPaymentAmount(payments)
        val newBulkPayment = bulkPaymentRepository.createBulkPayment(
                bulkPaymentAmount.toFloat(),
                payerId,
                receiverId
        ) ?: throw InternalError("There was a problem with saving bulk payment")

        paymentRepository.convertPaymentsToBulkPayment(paymentsIds, newBulkPayment.id)

        return newBulkPayment
    }

    fun updatePaymentStatus(bulkPaymentUpdateInput: UpdateBulkPaymentStatusInput, currentUserId: Long): BulkPayment {
        val bulkPayment = bulkPaymentRepository.findBulkPaymentById(bulkPaymentUpdateInput.id)
                ?: throw javax.persistence.EntityNotFoundException("bulk payment")

        requireCorrectBulkPaymentStatus(bulkPayment.status, bulkPaymentUpdateInput.status)

        if (bulkPaymentUpdateInput.status == BulkPaymentStatus.CONFIRMED) {
            requireBulkPaymentReceiver(bulkPayment.receiver, currentUserId)
        } else {
            requireBulkPaymentPayer(bulkPayment.payer, currentUserId)
        }

        bulkPaymentRepository.updateBulkPaymentStatus(bulkPayment.id, bulkPaymentUpdateInput.status)

        return bulkPayment.copy(status = bulkPaymentUpdateInput.status)
    }

    private fun requireBulkPaymentReceiver(receiver: User?, currentUserId: Long) {
        if (receiver == null) throw InternalError("Payment was not entirely fetched")
        if (receiver.id != currentUserId) throw UnauthorisedException()
    }

    private fun requireBulkPaymentPayer(payer: User?, currentUserId: Long) {
        if (payer == null) throw InternalError("Payment was not entirely fetched")
        if (payer.id != currentUserId) throw UnauthorisedException()
    }

    private fun requireCorrectBulkPaymentStatus(statusFrom: BulkPaymentStatus, statusTo: BulkPaymentStatus) {
        when (statusFrom) {
            BulkPaymentStatus.IN_PROGRESS -> {
                requireBulkPaymentStatuses(statusTo, listOf(BulkPaymentStatus.PAID))
            }
            BulkPaymentStatus.PAID -> {
                requireBulkPaymentStatuses(statusTo, listOf(BulkPaymentStatus.PAID, BulkPaymentStatus.IN_PROGRESS))
            }
            BulkPaymentStatus.CONFIRMED -> throw BulkPaymentStatusNotValid(BulkPaymentStatus.CONFIRMED)
        }
    }

    private fun requireBulkPaymentStatuses(statusTo: BulkPaymentStatus, availableStatuses: List<BulkPaymentStatus>) {
        if (!availableStatuses.contains(statusTo)) throw BulkPaymentStatusNotValid(statusTo)
    }

}

class BulkPaymentStatusNotValid(status: BulkPaymentStatus) : SimpleValidationException("Bulk Payment status was not valid, status is $status")
class TooPaymentParticipantsError() : SimpleValidationException("There was to many participants in the payments")
class InvalidPaymentState() : SimpleValidationException("Payment has not been initialized yet")
