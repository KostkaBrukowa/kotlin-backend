package com.example.graphql.domain.party

import com.example.graphql.domain.notification.NotificationService
import com.example.graphql.domain.partyrequest.PartyRequestRepository
import com.example.graphql.domain.user.UserTestBuilder
import spock.lang.Specification

class PartyServiceTest extends Specification {

    PartyRepository partyRepository = Mock()
    NotificationService notificationService = Mock()
    PartyRequestRepository partyRequestRepository = Stub()
    PartyService partyService = new PartyService(partyRepository, partyRequestRepository, notificationService)

    def "service should save new party with distinct participants and owner"() {
        given:
        Long userId = 42

        and:
        def newParty = PartyTestBuilder.defaultParty([
                participants: [
                        UserTestBuilder.defaultUser([id: 1]),
                        UserTestBuilder.defaultUser([id: 1]),
                        UserTestBuilder.defaultUser([id: 1])
                ],

        ])

        when:
        partyService.createParty(newParty, userId)

        then:
        1 * partyRepository.saveNewParty({ Party party ->
            (
                    party.owner.id == 42
                            && party.participants.size() == 2
                            && party.participants.any({ it.id == 1 })
                            && party.participants.any({ it.id == 42 })
            )
        }) >> PartyTestBuilder.defaultParty([owner: UserTestBuilder.defaultUser([id: userId])])
    }

    def "Should update a party with correct id"() {
        given:
        Long partyId = 420

        and:
        def partyToUpdate = PartyTestBuilder.defaultParty()

        when:
        partyService.updateParty(partyId, partyToUpdate)

        then:
        1 * partyRepository.updateParty({ Party party ->
            (
                    party.id == 420
            )
        })
    }
}
