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
        //TODO HASH CODE AND EQUALS
}
