package com.example.graphql.resolvers.user

import com.example.graphql.domain.party.PartyService
import com.example.graphql.domain.partyrequest.PartyRequestService
import com.example.graphql.domain.user.UserService
import com.example.graphql.schema.directives.Authenticated
import com.example.graphql.schema.directives.Roles
import com.expediagroup.graphql.spring.operations.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import javax.persistence.*


@Entity
data class Aaa(

        @Id
        @GeneratedValue
        val id: Long? = null,

        val name: String = "dfkals",

        @OneToMany(mappedBy = "aaa")
        val bbbList: Set<Bbb> = emptySet()
)

@Entity
data class Bbb(

        @Id
        @GeneratedValue
        val id: Long? = null,

        @ManyToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn
        val aaa: Aaa
)


interface BbbRepository : JpaRepository<Bbb, Long> {}
interface AaaRepository : JpaRepository<Aaa, Long> {}

@Component
class UserQuery(
        private val userService: UserService,
        private val partyRequestService: PartyRequestService,
        private val partyService: PartyService,
        private val bbbRepository: BbbRepository,
        private val aaaRepository: AaaRepository
) : Query {

    fun testManyToMany(): Boolean {
        val aaa = Aaa()
        val bbb1 = Bbb(aaa = aaa)
        val bbb2 = Bbb(aaa = aaa)
        val bbb3 = Bbb(aaa = aaa)

        aaaRepository.save(aaa)
        bbbRepository.save(bbb1)
        bbbRepository.save(bbb2)
        bbbRepository.save(bbb3)

        bbbRepository.findAll()

        return true
    }

    @Authenticated(role = Roles.USER)
    fun getUser(id: String): UserType? {
        return userService.getUserById(id)?.toResponse()
    }
//
//    @Authenticated(role = Roles.USER)
//    fun getUsersJoinedParties(userId: String): Nothing = TODO("")
}
