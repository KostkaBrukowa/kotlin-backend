package com.example.graphql.domain.messagegroup

import com.example.graphql.domain.message.PersistentMessage
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.user.PersistentUser
import javax.persistence.*

@Table(name = "message_groups")
@Entity
data class PersistentMessageGroup(
        @Id
        @GeneratedValue
        val id: Long? = null,

        @OneToMany(mappedBy = "messageGroup")
        val messages: List<PersistentMessage>,

        @ManyToMany()
        @JoinTable(name = "messagegroup_user")
        val users: List<PersistentUser>,

        @OneToOne(fetch = FetchType.LAZY, optional = true)
        val party: PersistentParty?
)
