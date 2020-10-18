package com.example.graphql.adapters.pgsql.notification

import com.example.graphql.domain.notification.Notification
import com.example.graphql.domain.user.PersistentUser
import org.hibernate.annotations.CreationTimestamp
import java.time.ZonedDateTime
import javax.persistence.*

@Table(name = "notifications")
@Entity
class PersistentNotification(
        @Id
        @GeneratedValue
        val id: Long = 0,

        @Column(nullable = false)
        @field:CreationTimestamp
        val createdAt: ZonedDateTime = ZonedDateTime.now(),

        val objectId: Long,

        @Column(nullable = true)
        val objectName: String? = null,

        val objectType: NotificationObjectType,

        @Enumerated(EnumType.STRING)
        val event: NotificationEvent,

        val isRead: Boolean = false,

        val isDeleted: Boolean = false,

        @ManyToOne(fetch = FetchType.LAZY,  cascade = [CascadeType.REMOVE])
        @JoinColumn(name = "actor_id", nullable = false)
        val actor: PersistentUser? = null,

        @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
        @JoinColumn(name = "receiver_id", nullable = false)
        val receiver: PersistentUser? = null
) {
    fun toDomain(): Notification {
        return Notification(
                id = id,
                createdAt = createdAt,
                objectId = objectId,
                objectName = objectName,
                objectType = objectType,
                event = event,
                isRead = isRead,
                isDeleted = isDeleted
        )
    }
}

enum class NotificationObjectType {
    EXPENSE,
    PARTY,
    PARTY_REQUEST,
    PAYMENT,
    BULK_PAYMENT
}

enum class NotificationEvent {
    NEW_MESSAGE,
    CREATION,
    MODIFICATION,
    DELETION,

    ACCEPTED,
    DECLINED,
    PAID,
    CONFIRMED,
    BULKED
}
