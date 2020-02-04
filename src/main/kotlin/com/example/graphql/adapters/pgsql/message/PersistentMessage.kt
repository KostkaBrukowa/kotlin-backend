package com.example.graphql.domain.message

import com.example.graphql.domain.messagegroup.PersistentMessageGroup
import com.example.graphql.domain.user.PersistentUser
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "messages")
@Entity
data class PersistentMessage(

        @Id
        @GeneratedValue
        val id: Long,

        val text: String,

        @Column(name = "send_date")
        val sendDate: ZonedDateTime,


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        val user: PersistentUser? = null,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "message_group_id", nullable = false)
        val messageGroup: PersistentMessageGroup
)
