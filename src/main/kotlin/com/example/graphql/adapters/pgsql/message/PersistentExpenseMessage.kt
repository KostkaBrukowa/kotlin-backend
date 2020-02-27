package com.example.graphql.adapters.pgsql.message

import com.example.graphql.adapters.pgsql.payment.PersistentBulkPayment
import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.message.PersistentMessage
import javax.persistence.*

@Table(name = "expense_messages")
@Entity
class PersistentExpenseMessage(

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "expense_id", nullable = false)
        val expense: PersistentExpense? = null
) : PersistentMessage()
