package com.example.graphql.domain.user

import com.example.graphql.domain.party.PartyTestBuilder
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class UserServiceTests extends Specification {
    UserRepository userRepository = Stub()
    UserService userService = new UserService(userRepository)

    def "service should response with correct user"() {
        given:
        userRepository.getUserById("1") >> UserTestBuilder.defaultUser([
                email: "test@email.com",
                id   : "1"
        ])

        when:
        def result = userService.getUserById("1")

        then:
        result.id == "1"
        result.email == "test@email.com"
    }

    def "service should respond with null when user is not found in database"() {
        given:
        userRepository.getUserById("1") >> null

        when:
        def result = userService.getUserById("1")

        then:
        result == null
    }

    @Unroll
    def "should return mapped parties to the users #partiesIds"() {
        given:
        def resultUsers = usersJoinedPartiesIds
                .entrySet()
                .stream()
                .map { it -> userWithJoinedParties(it.key, it.value) }
                .collect(Collectors.toList())

        and:
        userRepository.findAllPartiesParticipants(partiesIds) >> resultUsers

        when:
        def resultPartiesToParticipants = userService.findAllParticipantsByPartiesIds(partiesIds)

        then:
        resultMap.entrySet().stream().forEach { it -> mapContainsParticipants(resultPartiesToParticipants, it.key, it.value) }

        where:
        partiesIds                     | usersJoinedPartiesIds                               || resultMap
        ["1"] as Set<String>           | ["1": ["1", "2"]]                                   || ["1": ["1"]]
        ["1", "2", "3"] as Set<String> | ["1": ["1", "2"], "2": ["1"], "3": ["1", "2", "3"]] || ["1": ["1", "2", "3"], "2": ["1", "3"], "3": ["3"]]
    }

    def mapContainsParticipants(Map<String, List<User>> resultPartiesToParticipants, String partyId, List<String> participantsIds) {

        resultPartiesToParticipants[partyId].stream().forEach { it ->
            assert participantsIds.any { participantId -> it.id == participantId }
        }

        participantsIds.stream().forEach { expectedParticipantId ->
            assert resultPartiesToParticipants[partyId].any { participant -> participant.id == expectedParticipantId }
        }
    }

    def userWithJoinedParties(String userId, List<String> joinedPartiesIds) {
        def joinedParties = joinedPartiesIds
                .stream()
                .map { it -> PartyTestBuilder.defaultParty([id: it]) }
                .collect(Collectors.toList())

        return UserTestBuilder.defaultUser([
                id           : userId,
                joinedParties: joinedParties
        ])
    }
}
