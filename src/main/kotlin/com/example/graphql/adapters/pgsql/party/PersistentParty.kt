package com.example.graphql.domain.party

import com.example.graphql.domain.expense.PersistentExpense
import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.partyrequest.PersistentPartyRequest
import com.expediagroup.graphql.annotations.GraphQLID
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "parties")
@Entity
data class PersistentParty(
        @Id
        @GeneratedValue
        val id: Long? = null,

        @OneToOne(fetch = FetchType.LAZY)
        val messageGroup: PersistentMessageGroup,

        @OneToMany(mappedBy = "party")
        val partyRequests: List<PersistentPartyRequest>,

        @OneToMany(mappedBy = "party")
        val expenses: List<PersistentExpense>,

        val name: String,

        val description: String,

        @Column(name = "start_date")
        val startDate: ZonedDateTime,

        @Column(name = "end_date")
        val endDate: ZonedDateTime?
)
