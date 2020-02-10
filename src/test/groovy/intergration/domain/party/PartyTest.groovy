package intergration.domain.party

import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequest
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.party.PersistentParty
import com.example.graphql.domain.partyrequest.PartyRequestStatus
import com.example.graphql.domain.user.PersistentUser
import intergration.BaseIntegrationSpec
import org.apache.groovy.json.internal.LazyMap
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentUserTestBuilder.aClient
import static intergration.utils.builders.PersistentUserTestBuilder.defaultPersistentUser

class PartyTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentPartyRequestRepository partyRequestRepository

    def "Should return an error when user is not signed in"() {
        given:
        def getAllPartiesQuery = """getAllParties(userId: "42"){ id }"""

        when:
        def response = postQuery(getAllPartiesQuery, "getAllParties", true)

        then:
        response[0].errorType == "ExecutionAborted"
    }

    def "Should return all created parties created by user"() {
        given:
        authenticate("firstUser@gmail.com")

        and:
        def loggedInClient = defaultPersistentUser([id: baseUserId])
        def secondClient = aClient(userRepository)
        def thirdClient = aClient(userRepository)

        and:
        aParty([owner: secondClient], partyRepository)
        aParty([owner: thirdClient], partyRepository)
        aParty([owner: loggedInClient, name: 'logged party 1', participants: [secondClient, thirdClient]], partyRepository)
        aParty([owner: loggedInClient, name: 'logged party 2', participants: [secondClient, thirdClient]], partyRepository)

        and:
        def getAllPartiesQuery = """
            getAllParties(userId: "${baseUserId}") {
                name,
                owner { id }
                partyParticipants { id } 
                partyPartyRequests { id } 
            }
        """

        when:
        def response = postQuery(getAllPartiesQuery, "getAllParties") as ArrayList<LazyMap>

        then:
        response.size() == 2
        response.any { it -> it.owner.id == baseUserId && it.name == 'logged party 1' }
        response.any { it -> it.owner.id == baseUserId && it.name == 'logged party 2' }
        response.every { it -> it.partyParticipants.any { it.id == secondClient.id.toString() } }
        response.every { it -> it.partyParticipants.any { it.id == thirdClient.id.toString() } }
    }

    def "Should return single party given an id"() {
        given:
        authenticate("firstUser@gmail.com")

        and:
        def tenDaysFromNow = ZonedDateTime.now().plusDays(10)

        and:
        def partyId = aParty([
                name       : 'test name',
                description: 'test description',
                startDate  : tenDaysFromNow,
                owner      : aClient(userRepository)
        ], partyRepository).id

        and:
        def getSinglePartyQuery = """getSingleParty(partyId: "${partyId}"){ id, name, description, startDate }"""

        when:
        def partyResponse = postQuery(getSinglePartyQuery, "getSingleParty") as LazyMap

        then:
        partyResponse.id == partyId.toString()
        partyResponse.name == "test name"
        partyResponse.description == "test description"
        partyResponse.startDate == tenDaysFromNow.toString()
    }

    def "Should return party with only party owner as participant when create party mutation is called with no participants"() {
        given:
        authenticate()

        and:
        def tenDaysFromNow = ZonedDateTime.now().plusDays(10)
        def elevenDaysFromNow = ZonedDateTime.now().plusDays(11)

        and:
        def createPartyMutation = """
            createParty(
                newPartyInput: {
                  name: "test party"
                  description: "test description"
                  startDate: "${tenDaysFromNow}"
                  endDate: "${elevenDaysFromNow} "
                }
            ) { id }
        """

        and:
        String newPartyId = postMutation(createPartyMutation, "createParty").id

        when:
        PersistentParty partyResponse = partyRepository.getTopById(newPartyId.toLong())
        List<PersistentPartyRequest> partyRequestResponse = partyRequestRepository.findAllByPartyId(newPartyId.toLong())
        List<PersistentUser> participantsResponse = userRepository.findAllPartyParticipants(newPartyId.toLong())

        then:
        partyResponse.id == newPartyId.toLong()
        partyResponse.name == "test party"
        partyResponse.description == "test description"
        partyResponse.startDate == tenDaysFromNow
        partyResponse.endDate == elevenDaysFromNow
//        partyResponse.messageGroup.containsKey("id") TODO when messages are up
        partyRequestResponse.size() == 0
        participantsResponse.size() == 1
    }

    def "Should create party requests for all new participants"() {
        given:
        authenticate()

        and:
        def createPartyMutation = { String firstUserId, String secondUserId ->
            """
            createParty(
                newPartyInput: {
                  name: "test party"
                  description: "test description"
                  participants: [
                        "${firstUserId}"
                        "${secondUserId}"
                  ]
                  startDate:  "${ZonedDateTime.now().plusDays(10)}"
                }
            ) {
                id
            }
        """
        }

        and:
        String firstUserId = aClient(userRepository).id
        String secondUserId = aClient(userRepository).id

        and:
        String newPartyId = postMutation(createPartyMutation(firstUserId, secondUserId), "createParty").id

        when:

        def partyRequestsResponse = findPartyRequestsByPartyId(newPartyId)

        then:
        partyRequestsResponse.size() == 2
        partyRequestsResponse.any { it.user.id == firstUserId.toLong() }
        partyRequestsResponse.any { it.user.id == secondUserId.toLong() }
        partyRequestsResponse.every { it.status == PartyRequestStatus.IN_PROGRESS }
    }

    def "Should properly update a party"() {
        given:
        authenticate()

        and:
        def tenDaysFromNow = ZonedDateTime.now().plusDays(10)
        def elevenDaysFromNow = ZonedDateTime.now().plusDays(11)
        def twelveDaysFromNow = ZonedDateTime.now().plusDays(12)

        and:
        def partyId = aParty([
                name       : 'name before update',
                description: 'description before update',
                startDate  : tenDaysFromNow,
                endDate    : elevenDaysFromNow,
                owner      : baseUser
        ], partyRepository).id

        and:
        def updatePartyMutation = { String id ->
            """
            updateParty(
                id: "${id}" 
                editPartyInput: {
                  name: "updated party name"
                  description: "updated party description"
                  startDate: "${elevenDaysFromNow}"
                  endDate: "${twelveDaysFromNow}"
                }
            ) { name, description, startDate, endDate }
        """
        }

        when:
        postMutation(updatePartyMutation(partyId.toString()), "updateParty")

        and:
        def partyResponse = partyRepository.getTopById(partyId.toLong())

        then:
        partyResponse.name == "updated party name"
        partyResponse.description == "updated party description"
        partyResponse.startDate == elevenDaysFromNow
        partyResponse.endDate == twelveDaysFromNow
    }

    def "Should not delete a party when issuing user is not an owner"() {
        given:
        authenticate()

        and:
        def notAnOwner = aClient(userRepository)

        and:
        def partyId = aParty([
                name       : 'name',
                description: 'description',
                startDate  : ZonedDateTime.now().plusDays(10),
                owner      : notAnOwner
        ], partyRepository).id

        and:
        def deletePartyMutation = """deleteParty( id: "${partyId}" )"""

        when:
        def response = postMutation(deletePartyMutation, "deleteParty", true)

        then:
        response[0].errorType == "DataFetchingException"
    }

    def "Should delete a party when issuing user is an owner, and should remove users' participant status when party is deleted "() {
        given:
        authenticate()

        and:
        def owner = defaultPersistentUser([id: baseUserId])
        def participant1 = aClient(userRepository)
        def participant2 = aClient(userRepository)

        and:
        def partyId = aParty([
                name        : 'name',
                description : 'description',
                startDate   : ZonedDateTime.now().plusDays(10),
                owner       : owner,
                participants: [participant1, participant2]
        ], partyRepository).id

        and:
        def deletePartyMutation = """deleteParty( id: "${partyId}")"""

        when:
        postMutation(deletePartyMutation, "deleteParty")

        and:
        def partyResponse = partyRepository.getTopById(partyId.toLong())
        def participantsResponse = userRepository.findAllPartyParticipants(partyId)

        then:
        partyResponse == null
        participantsResponse.empty
    }

    @Unroll
    def "Should delete or return error(#error) a participant from a party when owner is #owner and participant is #participant "() {
        given:
        authenticate()

        and:
        def actualOwner = owner == "loggedInUser" ? baseUser : aClient(userRepository)
        def actualParticipant = participant == "loggedInUser" ? baseUser : aClient(userRepository)
        def aParty = aParty([owner: actualOwner, participants: [actualOwner, actualParticipant]], partyRepository)

        and:
        def removeParticipantMutation = ("""
            removeParticipant(
                partyId: "${aParty.id}",
                participantId: "${actualParticipant.id}"
            )
        """)

        when:
        def response = postMutation(removeParticipantMutation, "removeParticipant", error)

        and:
        def participantsResponse = userRepository.findAllPartyParticipants(aParty.id)

        then:
        if (error) {
            assert response[0].errorType == 'DataFetchingException'
            assert response[0].message.contains('User is not authorised to perform this action')
        } else {
            assert response == true
            assert participantsResponse.size() == 1
        }

        where:
        owner          | participant       | error
        "loggedInUser" | "otherClient"     | false
        "otherClient"  | "loggedInUser"    | false
        "otherClient"  | "differentClient" | true
    }

    def findPartyRequestsByPartyId(String partyId) {
        List<PersistentPartyRequest> partyRequestsResponse = partyRequestRepository.findAllByPartyId(partyId.toLong())
        partyRequestsResponse.forEach {
            Hibernate.initialize(it.user)
        }

        return partyRequestsResponse
    }
}
