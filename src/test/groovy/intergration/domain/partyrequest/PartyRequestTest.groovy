package intergration.domain.partyrequest

import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import intergration.BaseIntegrationSpec
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentPartyRequestTestBuilder.aPartyRequest
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class PartyRequestTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentPartyRequestRepository partyRequestRepository

    @Unroll
    def "Should add a participant to a party when user accepts/declines the request, and party request status should be #requestStatus "() {
        given:
        authenticate()

        and:
        def aParty = aParty(partyRepository)
        def aPartyRequest = aPartyRequest([user: baseUser, party: aParty], partyRequestRepository)

        and:
        def modifyPartyRequestStatus = ("""${mutation}(requestId: "${aPartyRequest.id}")""")

        when:
        postMutation(modifyPartyRequestStatus, "acceptParty")

        and:
        def actualPartyRequest = partyRequestRepository.findById(aPartyRequest.id)
        def actualPartyParticipants = userRepository.findAllPartyParticipants(aParty.id)

        then:
        actualPartyRequest.get().status == PartyRequestStatus.ACCEPTED
        actualPartyParticipants.size() == 1
        participantsSize == 0 || actualPartyParticipants.every { it.id == requestedClient.id }

        where:
        mutation              | participantsSize | requestStatus
        "acceptPartyRequest"  | 1                | PartyRequestStatus.ACCEPTED
        "declinePartyRequest" | 0                | PartyRequestStatus.DECLINED
    }

    def "Should return an error when different user tries to accept other user's request"() {
        given:
        authenticate()

        and:
        def differentUserThanCurrentlyLogged = aClient(userRepository)
        def aParty = aParty(partyRepository)
        def aPartyRequest = aPartyRequest([
                user : differentUserThanCurrentlyLogged,
                party: aParty
        ], partyRequestRepository)

        and:
        def acceptPartyRequestMutation = ("""${mutation}(requestId: "${aPartyRequest.id}")""")

        when:
        def response = postMutation(acceptPartyRequestMutation, "acceptParty", true)

        then:
        response[0].errorType == 'DataFetchingError'
        response[0].description.contains('User is not authorised to perform this action')

        where:
        mutation << ["acceptPartyRequest", "declinePartyRequest"]
    }

    @Unroll
    def "Party owner and party request owner has to be able to remove a party request but not any other entity"() {
        given:
        authenticate()

        and:
        def actualOwner = owner == "baseUser" ? baseUser : aClient(userRepository)
        def actualRequestee = requestee == "baseUser" ? baseUser : aClient(userRepository)
        def aParty = aParty([owner: actualOwner], partyRepository)
        def aPartyRequest = aPartyRequest([
                user : actualRequestee,
                party: aParty
        ], partyRequestRepository)

        and:
        def acceptPartyRequestMutation = ("""removePartyRequest(requestId: "${aPartyRequest.id}")""")

        when:
        def response = postMutation(acceptPartyRequestMutation, "acceptParty", true)
        def x = List.of('fds')

        then:
        if (error) {
            assert response[0].errorType == 'DataFetchingError'
            assert response[0].description.contains('User is not authorised to perform this action')
        } else {
            assert response == true
        }
        x hasSize


        where:
        loggedInEntity | owner         | requestee     | error
        "owner"        | "baseUser"    | "otherClient" | false
        "requestee"    | "otherClient" | "baseUser"    | false
        "otherUser"    | "otherClient" | "otherClient" | true
    }

    @Unroll
    def "Should send the party request to a user when party request is issued when the owner is the sender"() {
        given:
        authenticate()

        and:
        def clientToBeRequested = aClient(userRepository)
        def aParty = aParty([owner: ownerIsSender ? baseUser : aClient(userRepository)], partyRepository)

        and:
        def inviteUserToPartyMutation = ("""sendPartyRequest(userId: "${clientToBeRequested.id}", partyId: "${aParty.id}") { id }""")

        when:
        def response = postMutation(inviteUserToPartyMutation, "sendPartyRequest", !ownerIsSender)

        and:
        def actualPartyRequests = partyRequestRepository.findAllByUserId(clientToBeRequested.id)
        Hibernate.initialize(actualPartyRequests)

        then:
        if (ownerIsSender) {
            assert actualPartyRequests.size() == 1
            assert actualPartyRequests.get(0).user.id == clientToBeRequested.id
        } else {
            assert response[0].errorType == 'DataFetchingException'
            assert response[0].descriptoin == 'User is not authorised to perform this action'
        }

        where:
        ownerIsSender << [true, false]
    }

    def "Should not sent new request when same request has already been issued"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)
        def aClient = aClient(userRepository)

        and:
        def alreadyIssuedRequest = aPartyRequest([party: aParty, user: aClient], partyRequestRepository)

        and:
        def inviteUserToPartyMutation = ("""sendPartyRequest(userId: "${aClient.id}", partyId: "${aParty.id}") { id }""")

        when:
        postMutation(inviteUserToPartyMutation, "sendPartyRequest")

        and:
        def actualPartyRequests = partyRequestRepository.findAllByUserId(aClient.id)

        then:
        actualPartyRequests.size() == 1
        actualPartyRequests.get(0).id == alreadyIssuedRequest.id
    }

    def "Should not sent new request to the owner"() {
        given:
        authenticate()

        and:
        def aParty = aParty([owner: baseUser], partyRepository)

        and:
        def inviteUserToPartyMutation = ("""sendPartyRequest(userId: "${baseUser.id}", partyId: "${aParty.id}") { id }""")

        when:
        postMutation(inviteUserToPartyMutation, "sendPartyRequest")

        and:
        def actualPartyRequests = partyRequestRepository.findAllByUserId(baseUser.id)

        then:
        actualPartyRequests.empty
    }

    def "Should not send a request when party has already been finished"() {
        given:
        authenticate()

        and:
        def aClient = aClient(userRepository)
        def aParty = aParty([owner: baseUser, endDate: ZonedDateTime.now().minusDays(1)], partyRepository)

        and:
        def inviteUserToPartyMutation = ("""sendPartyRequest(userId: "${aClient.id}", partyId: "${aParty.id}") { id }""")

        when:
        def response = postMutation(inviteUserToPartyMutation, "sendPartyRequest")

        and:
        def actualPartyRequests = partyRequestRepository.findAllByUserId(aClient.id)

        then:
        actualPartyRequests.empty
        response[0].errorType == 'DataFetchingException'
    }
}
