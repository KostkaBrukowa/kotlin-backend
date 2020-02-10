package com.example.graphql.domain.user

import com.example.graphql.domain.party.PartyRepository
import com.example.graphql.resolvers.user.UserType
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

import static com.example.graphql.domain.party.PartyTestBuilder.defaultParty
import static com.example.graphql.domain.user.UserTestBuilder.defaultUser

class UserServiceTests extends Specification {
    UserRepository userRepository = Stub()
    UserService userService = new UserService(userRepository)

    def "service should response with correct user"() {
        given:
        userRepository.getUserById(1) >> defaultUser([
                email: "test@email.com",
                id   : "1"
        ])

        when:
        def result = userService.getUserById(1)

        then:
        result.id == 1
        result.email == "test@email.com"
    }

    def "service should respond with null when user is not found in database"() {
        given:
        userRepository.getUserById(1) >> null

        when:
        def result = userService.getUserById(1)

        then:
        result == null
    }
}
