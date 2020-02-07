package intergration.domain.partyrequest

import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import intergration.BaseIntegrationSpec
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Unroll

import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentPartyRequestTestBuilder.aPartyRequest
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class PartyRequestMutationTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentPartyRequestRepository partyRequestRepository

    @Unroll
    def "Should add a participant to a party when user #mutation, and party request status should be #requestStatus "() {
        given:
        authenticate()

        and:
        def partyOwner = aClient(userRepository)
        def aParty = aParty([owner: partyOwner, participants: [partyOwner]], partyRepository)
        def aPartyRequest = aPartyRequest([user: baseUser, party: aParty], partyRequestRepository)

        and:
        def modifyPartyRequestStatus = ("""${mutation}(partyRequestId: "${aPartyRequest.id}")""")

        when:
        postMutation(modifyPartyRequestStatus, "acceptParty")

        and:
        def actualPartyRequest = partyRequestRepository.findById(aPartyRequest.id)
        def actualPartyParticipants = userRepository.findAllPartyParticipants(aParty.id)

        then:
        actualPartyRequest.get().status == requestStatus
        actualPartyParticipants.size() == participantsSize
        if (requestStatus == PartyRequestStatus.ACCEPTED) {
            assert actualPartyParticipants.any { it.id == baseUser.id }
        } else {
            assert participantsSize == 1
        }

        where:
        mutation              | participantsSize | requestStatus
        "acceptPartyRequest"  | 2                | PartyRequestStatus.ACCEPTED
        "declinePartyRequest" | 1                | PartyRequestStatus.DECLINED
    }

    @Unroll
    def "Should return an error when different user tries to #mutation other user's request"() {
        given:
        authenticate()

        and:
        def differentUserThanCurrentlyLogged = aClient(userRepository)

        def aParty = aParty(owner: baseUser, partyRepository)
        def aPartyRequest = aPartyRequest([
                user  : differentUserThanCurrentlyLogged,
                party : aParty,
                status: mutation == "acceptPartyRequest" ? PartyRequestStatus.DECLINED : PartyRequestStatus.ACCEPTED
        ], partyRequestRepository)

        and:
        def changeRequestStatusMutation = ("""${mutation}(partyRequestId: "${aPartyRequest.id}")""")

        when:
        def response = postMutation(changeRequestStatusMutation, "acceptPartyRequest", true)

        then:
        response[0].errorType == 'DataFetchingException'
        response[0].message.contains('User is not authorised to perform this action')

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
        def removePartyRequestMutation = ("""removePartyRequest(partyRequestId: "${aPartyRequest.id}")""")

        when:
        def response = postMutation(removePartyRequestMutation, "removePartyRequest", error)

        then:
        if (error) {
            assert response[0].errorType == 'DataFetchingException'
            assert response[0].message.contains('User is not authorised to perform this action')
        } else {
            assert response == true
        }


        where:
        owner         | requestee     | error
        "baseUser"    | "otherClient" | false
        "otherClient" | "baseUser"    | false
        "otherClient" | "otherClient" | true
    }

    @Unroll
    def "Should send [#ownerIsSender] the party request to a user when party request is issued when the owner is [#ownerIsSender] the sender"() {
        given:
        authenticate()

        and:
        def clientToBeRequested = aClient(userRepository)
        def aParty = aParty([owner: ownerIsSender ? baseUser : aClient(userRepository)], partyRepository)

        and:
        def inviteUserToPartyMutation = ("""sendPartyRequest(requestReceiverId: "${clientToBeRequested.id}", partyId: "${aParty.id}") { id }""")

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
            assert response[0].message.contains("User is not authorised to perform this action")
        }

        where:
        ownerIsSender << [true, false]
    }

    def "Should not sent new request when same request has already been issued"() {
        given:
        authenticate()

        and:
        def aClient = aClient(userRepository)
        def aParty = aParty([owner: baseUser, participants: [aClient]], partyRepository)

        and:
        def alreadyIssuedRequest = aPartyRequest([party: aParty, user: aClient], partyRequestRepository)

        and:
        def inviteUserToPartyMutation = ("""sendPartyRequest(requestReceiverId: "${aClient.id}", partyId: "${aParty.id}") { id }""")

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
        def inviteUserToPartyMutation = ("""sendPartyRequest(requestReceiverId: "${baseUser.id}", partyId: "${aParty.id}") { id }""")

        when:
        def response = postMutation(inviteUserToPartyMutation, "sendPartyRequest")

        and:
        def actualPartyRequests = partyRequestRepository.findAllByUserId(baseUser.id)

        then:
        actualPartyRequests.empty
        response == null
    }

    @Ignore
    def "Should not send a request when party has already been finished"() {
        given:
        authenticate()

        and:
        def aClient = aClient(userRepository)
        def aParty = aParty([owner: baseUser, endDate: ZonedDateTime.now().minusDays(1)], partyRepository)

        and:
        def inviteUserToPartyMutation = ("""sendPartyRequest(requestReceiverId: "${aClient.id}", partyId: "${aParty.id}") { id }""")

        when:
        def response = postMutation(inviteUserToPartyMutation, "sendPartyRequest")

        and:
        def actualPartyRequests = partyRequestRepository.findAllByUserId(aClient.id)

        then:
        actualPartyRequests.empty
        response[0].errorType == 'DataFetchingException'
    }
}
