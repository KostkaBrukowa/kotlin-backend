package com.example.graphql.adapters.pgsql.notification

import com.example.graphql.domain.user.PersistentUser
import org.hibernate.annotations.CreationTimestamp
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "parties")
@Entity
class PersistentNotification(
        @Id
        @GeneratedValue
        val id: Long = 0

//        val text: String = "",
//
//        @field:CreationTimestamp
//        val createdAt: ZonedDateTime? = null,
//
//        val objectId: Long,
//
//        val objectType: NotificationObjectType,
//
//
//        @ManyToOne(fetch = FetchType.LAZY)
//        @JoinColumn(name = "user_id", nullable = false)
//        val actor: PersistentUser? = null
)

enum class NotificationObjectType {
        NEW_MESSAGE,

}
