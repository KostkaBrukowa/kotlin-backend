package com.example.graphql.resolvers.payment

import com.example.graphql.domain.payment.PaymentDataLoaderService
import com.example.graphql.resolvers.expense.ExpenseType
import com.example.graphql.resolvers.message.MessageResponseType
import com.example.graphql.resolvers.message.MessageType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.DataFetcher
import com.example.graphql.resolvers.utils.dataLoader
import org.dataloader.DataLoader
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component


const val PAYMENT_PAYER_LOADER_NAME = "PaymentPayerDataFetcher"
const val PAYMENT_EXPENSE_LOADER_NAME = "PaymentExpenseDataFetcher"
const val PAYMENT_MESSAGES_LOADER_NAME = "PaymentMessagesDataFetcher"
const val BULK_PAYMENT_PAYER_LOADER_NAME = "BulkPaymentPayerDataFetcher"
const val BULK_PAYMENT_RECEIVER_LOADER_NAME = "BulkPaymentReceiverDataFetcher"
const val BULK_PAYMENT_PAYMENTS_LOADER_NAME = "BulkPaymentPaymentsDataFetcher"
const val BULK_PAYMENT_MESSAGES_LOADER_NAME = "BulkPaymentMessagesDataFetcher"

@Component
class PaymentDataLoadersBuilder(private val paymentDataLoaderService: PaymentDataLoaderService) {

    fun getPayersDataLoader(): DataLoader<String, UserType> {
        return dataLoader { ids -> paymentDataLoaderService.paymentToUserDataLoaderMap(ids) }
    }

    fun getExpensesDataLoader(): DataLoader<String, ExpenseType> {
        return dataLoader { ids -> paymentDataLoaderService.paymentToExpenseDataLoaderMap(ids) }
    }

    fun getMessagesDataLoader(): DataLoader<String, List<MessageResponseType>> {
        return dataLoader { ids -> paymentDataLoaderService.paymentToMessageDataLoaderMap(ids) }
    }
}

@Component
class BulkPaymentDataLoadersBuilder(private val paymentDataLoaderService: PaymentDataLoaderService) {

    fun getPayersDataLoader(): DataLoader<String, UserType> {
        return dataLoader { ids -> paymentDataLoaderService.bulkPaymentToPayerDataLoaderMap(ids) }
    }

    fun getReceiversDataLoader(): DataLoader<String, UserType> {
        return dataLoader { ids -> paymentDataLoaderService.bulkPaymentToReceiverDataLoaderMap(ids) }
    }

    fun getPaymentsDataLoader(): DataLoader<String, List<PaymentType>> {
        return dataLoader { ids -> paymentDataLoaderService.bulkPaymentToPaymentsDataLoaderMap(ids) }
    }

    fun getMessagesDataLoader(): DataLoader<String, List<MessageResponseType>> {
        return dataLoader { ids -> paymentDataLoaderService.bulkPaymentToMessageDataLoaderMap(ids) }
    }
}

@Component(PAYMENT_EXPENSE_LOADER_NAME)
@Scope("prototype")
class PaymentExpenseDataFetcher : DataFetcher(PAYMENT_EXPENSE_LOADER_NAME)

@Component(PAYMENT_PAYER_LOADER_NAME)
@Scope("prototype")
class PaymentPayerDataFetcher : DataFetcher(PAYMENT_PAYER_LOADER_NAME)

@Component(PAYMENT_MESSAGES_LOADER_NAME)
@Scope("prototype")
class PaymentMessagesDataFetcher : DataFetcher(PAYMENT_MESSAGES_LOADER_NAME)

@Component(BULK_PAYMENT_RECEIVER_LOADER_NAME)
@Scope("prototype")
class BulkPaymentReceiverDataFetcher : DataFetcher(BULK_PAYMENT_RECEIVER_LOADER_NAME)

@Component(BULK_PAYMENT_PAYER_LOADER_NAME)
@Scope("prototype")
class BulkPaymentPayerDataFetcher : DataFetcher(BULK_PAYMENT_PAYER_LOADER_NAME)

@Component(BULK_PAYMENT_PAYMENTS_LOADER_NAME)
@Scope("prototype")
class BulkPaymentPaymentsDataFetcher : DataFetcher(BULK_PAYMENT_PAYMENTS_LOADER_NAME)

@Component(BULK_PAYMENT_MESSAGES_LOADER_NAME)
@Scope("prototype")
class BulkPaymentMessagesDataFetcher : DataFetcher(BULK_PAYMENT_MESSAGES_LOADER_NAME)
