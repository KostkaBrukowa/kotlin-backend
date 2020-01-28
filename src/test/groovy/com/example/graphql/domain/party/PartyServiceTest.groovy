package com.example.graphql.domain.party


import com.example.graphql.domain.party.PartyTestBuilder
import com.example.graphql.domain.user.UserRepository
import com.example.graphql.domain.user.UserTestBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import spock.lang.Specification

class PartyServiceTest extends Specification {

    PartyRepository partyRepository = Mock()
    UserRepository userRepository = Stub()
    PartyService partyService = new PartyService(partyRepository, userRepository)

    def "service should save new party with distinct participants and owner"() {
        given:
        String userId = '42'

        when:
        partyService.createParty(PartyTestBuilder.defaultParty([
                participants: [
                        UserTestBuilder.defaultUser([id: '1']),
                        UserTestBuilder.defaultUser([id: '1']),
                        UserTestBuilder.defaultUser([id: '1'])
                ]
        ]), userId)

        then:
        1 * partyRepository.saveNewParty({ Party party ->
            (
                    party.owner.id == '42'
                            && party.participants.size() == 2
                            && party.participants.any { it.id == '1' }
                            && party.participants.any { it.id == '42' }
            )
        })
    }

    def "Should update a party with correct id"() {
        given:
        String partyId = '420'

        and:
        def partyToUpdate = PartyTestBuilder.defaultParty()

        when:
        partyService.updateParty(partyId, partyToUpdate)

        then:
        1 * partyRepository.updateParty({ Party party ->
            (
                    party.id == '420'
            )
        })
    }
}
