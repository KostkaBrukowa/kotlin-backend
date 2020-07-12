package com.example.graphql.resolvers.expense

import com.example.graphql.domain.expense.Expense
import com.example.graphql.domain.expense.ExpenseStatus
import com.example.graphql.resolvers.message.MessageResponseType
import com.example.graphql.resolvers.partyrequest.PartyRequestType
import com.example.graphql.resolvers.payment.PaymentType
import com.example.graphql.resolvers.user.UserType
import com.example.graphql.resolvers.utils.GQLResponseType
import com.expediagroup.graphql.annotations.GraphQLID
import org.hibernate.validator.constraints.Length
import java.time.ZonedDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive


data class ExpenseType(
        @GraphQLID
        override val id: String = "0",

        val name: String,

        val amount: Float,

        val expenseDate: ZonedDateTime,

        val description: String,

        val expenseStatus: ExpenseStatus

) : GQLResponseType {

    lateinit var expensePayer: UserType

    lateinit var expenseParty: PartyRequestType

    lateinit var expensePayments: List<PaymentType>

    lateinit var expenseMessages: List<MessageResponseType>
}

fun Expense.toResponse() = ExpenseType(
        id = id.toString(),
        name = name,
        amount = amount,
        expenseDate = expenseDate,
        description = description,
        expenseStatus = expenseStatus
)

data class NewExpenseInput(

        @field:Length(min = 3, max = 256)
        val name: String,

        @field:Positive
        @field:Min(value = 1)
        val amount: Float,

        @field:PastOrPresent
        val expenseDate: ZonedDateTime,

        @field:Length(min = 3, max = 256)
        val description: String,

        val partyId: String,

        val participants: List<Long>
)

data class UpdateExpenseInput(

        val id: String,

        @field:Length(min = 3, max = 256)
        val name: String,

        @field:PastOrPresent
        val expenseDate: ZonedDateTime,

        @field:Length(min = 3, max = 256)
        val description: String
)

data class UpdateExpenseAmountInput(

        val id: String,

        @field:Positive
        @field:Min(value = 1)
        val amount: Float
)

data class UpdateExpenseStatusInput(

        val id: String,

        val expenseStatus: ExpenseStatus
)

