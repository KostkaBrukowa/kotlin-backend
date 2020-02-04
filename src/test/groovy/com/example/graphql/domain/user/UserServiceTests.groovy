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
    PartyRepository partyRepository = Stub()
    UserService userService = new UserService(userRepository, partyRepository)

    def "service should response with correct user"() {
        given:
        userRepository.getUserById(1) >> UserTestBuilder.defaultUser([
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

    @Unroll
    def "should return mapped parties to the users"() {
        given:
        def partiesIds = parties.stream().map { it.id }.collect(Collectors.toSet())

        and:
        partyRepository.findPartiesWithParticipants(partiesIds) >> parties

        when:
        def resultPartiesToParticipants = userService.findAllParticipantsByPartiesIds(partiesIds)

        then:
        resultMap
                .entrySet()
                .stream()
                .forEach {
                    it -> mapContainsParticipants(resultPartiesToParticipants, it.key, it.value)
                }

        where:
        parties                                                       | resultMap
        [defaultParty([id: 1, participants: [defaultUser([id: 1])]])] | [1: [1]]
        [
                defaultParty([id: 1, participants: [
                        defaultUser([id: 1]),
                        defaultUser([id: 2]),
                        defaultUser([id: 3])
                ]]),
                defaultParty([id: 2, participants: [
                        defaultUser([id: 1]),
                        defaultUser([id: 3])
                ]]),
                defaultParty([id: 3, participants: [
                        defaultUser([id: 3]),
                ]])
        ]                                                             | [1: [1, 2, 3], 2: [1, 3], 3: [3]]
    }

    def mapContainsParticipants(Map<Long, List<UserType>> resultPartiesToParticipants, Long partyId, List<Long> participantsIds) {
        assert resultPartiesToParticipants[partyId] instanceof List

        resultPartiesToParticipants[partyId].stream().forEach { it ->
            assert participantsIds.any { participantId -> it.id.toLong() == participantId }
        }

        participantsIds.stream().forEach { expectedParticipantId ->
            assert resultPartiesToParticipants[partyId].any { participant -> participant.id.toLong() == expectedParticipantId }
        }
    }
}
