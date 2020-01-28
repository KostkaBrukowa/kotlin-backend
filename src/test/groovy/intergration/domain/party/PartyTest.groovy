package intergration.domain.party

import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import intergration.BaseIntegrationSpec
import org.apache.groovy.json.internal.LazyMap
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

import java.time.ZonedDateTime

import static intergration.utils.builders.PersistentPartyTestBuilder.aPartyWithProps
import static intergration.utils.builders.PersistentUserTestBuilder.aClient
import static intergration.utils.builders.PersistentUserTestBuilder.defaultPersistentUser

class PartyTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

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
        aPartyWithProps([owner: secondClient], partyRepository)
        aPartyWithProps([owner: thirdClient], partyRepository)
        aPartyWithProps([owner: loggedInClient, name: 'logged party 1'], partyRepository)
        aPartyWithProps([owner: loggedInClient, name: 'logged party 2'], partyRepository)

        and:
        def getAllPartiesQuery = """getAllParties(userId: "${baseUserId}"){ name, owner { id } }"""

        when:
        def response = postQuery(getAllPartiesQuery, "getAllParties") as ArrayList<LazyMap>

        then:
        response.size() == 2
        response.any { it -> it.owner.id == baseUserId && it.name == 'logged party 1'}
        response.any { it -> it.owner.id == baseUserId && it.name == 'logged party 2'}
    }

    def "Should return a created party given an id"() {
        given:
        authenticate("firstUser@gmail.com")

        and:
        def tenDaysFromNow = ZonedDateTime.now().plusDays(10)

        and:
        def partyId = aPartyWithProps([
                name       : 'test name',
                description: 'test description',
                startDate  : tenDaysFromNow,
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
        def partyResponse = partyRepository.getTopById(newPartyId.toLong())

        then:
        partyResponse.id == newPartyId.toLong()
        partyResponse.name == "test party"
        partyResponse.description == "test description"
        partyResponse.startDate == tenDaysFromNow
        partyResponse.endDate == elevenDaysFromNow
//        partyResponse.messageGroup.containsKey("id")
        partyResponse.participants.size() == 1
        partyResponse.partyRequests.size() == 0
    }

    @Ignore
    def "Should return party requests for all participants"() {
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
        def getSinglePartyQuery = { String id -> """getSingleParty(partyId: "${id}"){ id, partyRequests { user { id } } }""" }

        and:
        String firstUserId = aClient(userRepository).id
        String secondUserId = aClient(userRepository).id

        and:
        String newPartyId = postMutation(createPartyMutation(firstUserId, secondUserId), "createParty").id

        when:
        def partyResponse = postQuery(getSinglePartyQuery(newPartyId), "getSingleParty") as LazyMap

        then:
        partyResponse.partyRequests.size() == 2
        partyResponse.partyRequests[0].user.id == firstUserId
        partyResponse.partyRequests[1].user.id == secondUserId
        partyResponse.partyRequests[0].status == "IN_PROGRESS"
        partyResponse.partyRequests[1].status == "IN_PROGRESS"
    }

    def "Should properly update a party"() {
        given:
        authenticate()

        and:
        def tenDaysFromNow = ZonedDateTime.now().plusDays(10)
        def elevenDaysFromNow = ZonedDateTime.now().plusDays(11)
        def twelveDaysFromNow = ZonedDateTime.now().plusDays(12)

        and:
        def partyId = aPartyWithProps([
                name       : 'name before update',
                description: 'description before update',
                startDate  : tenDaysFromNow,
                endDate    : elevenDaysFromNow
        ], partyRepository).id

        and:
        def updatePartyMutation = { String id ->
            """
            updateParty(
                id: "${id}" 
                newPartyInput: {
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
}
