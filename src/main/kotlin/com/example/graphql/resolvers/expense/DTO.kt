package com.example.graphql.resolvers.expense

import org.hibernate.validator.constraints.Length
import java.time.ZonedDateTime
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive


data class NewExpenseInput(

        @field:Positive
        val amount: Float,

        @field:PastOrPresent
        val expenseDate: ZonedDateTime,

        @field:Length(min = 3, max = 256)
        val description: String,

        val partyId: Long,

        val participants: List<Long>
)

