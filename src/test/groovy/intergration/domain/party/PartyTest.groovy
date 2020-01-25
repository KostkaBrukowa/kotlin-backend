package intergration.domain.party

import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.domain.user.User
import intergration.BaseIntegrationSpec
import org.apache.commons.collections.map.LazyMap
import org.springframework.beans.factory.annotation.Autowired

import static intergration.utils.builders.PersistentUserTestBuilder.aClientWithId

class PartyTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository repository

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
        def createPartyMutation = { String name ->
            """
            createParty(
                newPartyInput: {
                  name: "${name}"
                  description: "test description"
                  startDate: "01.01.2020"
                }
            ) { id }
        """
        }

        and:
        postMutation(createPartyMutation("first user party"), "createParty")

        and:
        authenticate("secondUser@gmail.com")

        and:
        postMutation(createPartyMutation("second user party 1"), "createParty")
        postMutation(createPartyMutation("second user party 2"), "createParty")

        and:
        def getAllPartiesQuery = """getAllParties(userId: "${baseUserId}"){ name }"""

        when:
        def response = postQuery(getAllPartiesQuery, "getAllParties") as User[]

        then:
        response.any({ User it -> it.name == "second user party 1" })
        response.any({ User it -> it.name == "second user party 2" })
    }

    def "Should return party with only party owner when create party mutation is called with no participants"() {
        given:
        authenticate()

        and:
        def createPartyMutation = """
            createParty(
                newPartyInput: {
                  name: "test party"
                  description: "test description"
                  startDate: "01.01.2020"
                  endDate: "02.01.2020"
                }
            ) {
                id
                name
                description
                participants {
                    id
                }
                messageGroup {
                    id
                }
                partyRequests {
                    id
                }
            }
        """

        and:
        def getSinglePartyQuery = { String id -> """getSingleParty(id: "${id}"){ id}""" }

        and:
        String newPartyId = postMutation(createPartyMutation, "createParty").id

        when:
        def partyResponse = postQuery(getSinglePartyQuery(newPartyId), "getSingleParty") as LazyMap

        then:
        partyResponse.containsKey("id")
        partyResponse.name == "test party"
        partyResponse.description == "test description"
        partyResponse.startDate == "01.01.2020"
        partyResponse.messageGroup.containsKey("id")
        partyResponse.participants.length == 1
        partyResponse.partyRequests.length == 0
    }

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
                        ${firstUserId}
                        ${secondUserId}
                  ]
                  startDate: "01.01.2020"
                }
            ) {
                partyRequests {
                    id
                    status
                }
            }
        """
        }

        and:
        def getSinglePartyQuery = { String id -> """getSingleParty(id: "${id}"){ id}""" }

        and:
        String firstUserId = aClientWithId(42, repository).id
        String secondUserId = aClientWithId(42, repository).id

        and:
        String newPartyId = postMutation(createPartyMutation(firstUserId, secondUserId), "createParty").id

        when:
        def partyResponse = postQuery(getSinglePartyQuery(newPartyId), "getSingleParty") as LazyMap

        then:
        partyResponse.partyRequests.length == 2
        partyResponse.partyRequests[0].id == firstUserId
        partyResponse.partyRequests[1].id == secondUserId
        partyResponse.partyRequests[0].status == "IN_PROGRESS"
        partyResponse.partyRequests[1].status == "IN_PROGRESS"
    }

    def "Should properly update a party"() {
        given:
        authenticate()

        and:
        def createPartyMutation = """
            createParty(
                newPartyInput: {
                  name: "test party"
                  description: "test description"
                  startDate: "01.01.2020"
                  endDate: "02.01.2020"
                }
            ) { id }
        """
        and:
        def updatePartyMutation = { String id -> """
            updateParty(
                updatePartyInput: {
                  id: "${id}" 
                  name: "updated party name"
                  description: "updated party description"
                  startDate: "01.01.2021"
                  endDate: "02.01.2021"
                }
            ) { name, description, startDate, endDate }
        """ }

        and:
        def getSinglePartyQuery = { String id -> """getSingleParty(id: "${id}"){ id }""" }

        and:
        String newPartyId = postMutation(createPartyMutation, "createParty").id

        and:
        postMutation(updatePartyMutation(newPartyId), "updateParty")

        when:
        def partyResponse = postQuery(getSinglePartyQuery(newPartyId), "getSingleParty") as LazyMap

        then:
        partyResponse.name == "updated test party"
        partyResponse.description == "updated test description"
        partyResponse.startDate == "01.01.2021"
        partyResponse.startDate == "02.01.2021"
    }
}
