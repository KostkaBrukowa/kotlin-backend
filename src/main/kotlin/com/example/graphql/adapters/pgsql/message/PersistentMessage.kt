package com.example.graphql.domain.message

import com.example.graphql.domain.user.PersistentUser
import org.hibernate.annotations.CreationTimestamp
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class PersistentMessage(
        @Id
        @GeneratedValue
        val id: Long = 0,

        var text: String = "",

        @field:CreationTimestamp
        var createdAt: ZonedDateTime? = null,


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        var user: PersistentUser? = null
) {
        fun toDomain() = Message(
                id = this.id,
                text = this.text,
                sendDate = this.createdAt ?: ZonedDateTime.now(),
                user = this.user!!.toDomain()
        )

        override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + text.hashCode()
                result = 31 * result + createdAt.hashCode()

                return result
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other?.javaClass != javaClass) return false

                other as PersistentMessage

                return this.id == other.id && this.text == other.text && this.createdAt == other.createdAt
        }
}
