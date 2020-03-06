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

        val amount: Float,

        val expenseDate: ZonedDateTime,

        val description: String? = null,

        val expenseStatus: ExpenseStatus

) : GQLResponseType {

    lateinit var expensePayer: UserType

    lateinit var expenseParty: PartyRequestType

    lateinit var expensePayments: List<PaymentType>

    lateinit var expenseMessages: List<MessageResponseType>
}

fun Expense.toResponse() = ExpenseType(
        id = this.id.toString(),
        amount = this.amount,
        expenseDate = this.expenseDate,
        description = this.description,
        expenseStatus = this.expenseStatus
)

data class NewExpenseInput(

        @field:Positive
        @field:Min(value = 1)
        val amount: Float,

        @field:PastOrPresent
        val expenseDate: ZonedDateTime,

        @field:Length(min = 3, max = 256)
        val description: String,

        val partyId: Long,

        val participants: List<Long>
)

data class UpdateExpenseInput(

        val id: Long,

        @field:PastOrPresent
        val expenseDate: ZonedDateTime,

        @field:Length(min = 3, max = 256)
        val description: String
)

data class UpdateExpenseAmountInput(

        val id: Long,

        @field:Positive
        @field:Min(value = 1)
        val amount: Float
)

data class UpdateExpenseStatusInput(

        val id: Long,

        val expenseStatus: ExpenseStatus
)

