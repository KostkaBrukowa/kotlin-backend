package intergration.domain.partyrequest

import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import static intergration.utils.builders.PersistentPartyRequestTestBuilder.aPartyRequest
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class PartyRequestQueryTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentPartyRequestRepository partyRequestRepository

    def "Should return all party requests for a party"() {
        given:
        authenticate()

        and:
        def owner = aClient(userRepository)
        def aParty = aParty([owner: owner, participants: [owner]], partyRepository)
        def firstPartyRequest = aPartyRequest([user: aClient(userRepository), party: aParty], partyRequestRepository)
        def secondPartyRequest = aPartyRequest([user: aClient(userRepository), party: aParty], partyRequestRepository)
        def thirdPartyRequest = aPartyRequest([user: aClient(userRepository), party: aParty], partyRequestRepository)

        and:
        def getPartRequestsForPartyQuery = ("""
            getPartyRequestsForParty(partyId: "${aParty.id}") {
                 id,
                 partyRequestReceiver { id },
                 partyRequestParty { id }
             }
        """)

        when:
        def response = postQuery(getPartRequestsForPartyQuery, "getPartyRequestsForParty")

        then:
        response.size() == 3
        response.any { it.partyRequestReceiver.id.toLong() == firstPartyRequest.user.id }
        response.any { it.partyRequestReceiver.id.toLong() == secondPartyRequest.user.id }
        response.any { it.partyRequestReceiver.id.toLong() == thirdPartyRequest.user.id }
        response.every { it.partyRequestParty.id.toLong() == aParty.id }
    }

    def "Should return all party requests for a user"() {
        given:
        authenticate()

        and:
        def firstParty = aParty([owner: aClient(userRepository)], partyRepository)
        def secondParty = aParty([owner: aClient(userRepository)], partyRepository)
        def thirdParty = aParty([owner: aClient(userRepository)], partyRepository)
        aPartyRequest([user: baseUser, party: firstParty], partyRequestRepository)
        aPartyRequest([user: baseUser, party: secondParty], partyRequestRepository)
        aPartyRequest([user: baseUser, party: thirdParty], partyRequestRepository)

        and:
        def getPartRequestsForPartyQuery = ("""
            getPartyRequestsForUser(userId: "${baseUser.id}") { 
                id,
                partyRequestReceiver { id },
                partyRequestParty { id }
            }
        """)

        when:
        def response = postQuery(getPartRequestsForPartyQuery, "getPartyRequestsForUser")

        then:
        response.size() == 3
        response.any { it.partyRequestParty.id.toLong() == firstParty.id }
        response.any { it.partyRequestParty.id.toLong() == secondParty.id }
        response.any { it.partyRequestParty.id.toLong() == thirdParty.id }
        response.every { it.partyRequestReceiver.id.toLong() == baseUser.id }
    }

    def "Should return an error when different user tries to read someone else's party requests"() {
        given:
        authenticate()

        and:
        def getPartRequestsForPartyQuery = ("""getPartyRequestsForUser(userId: "${aClient(userRepository).id}") { id }""")

        when:
        def response = postQuery(getPartRequestsForPartyQuery, "getPartyRequestsForUser", true)

        then:
        response[0].message.contains('User is not authorised to perform this action')
    }
}
