package intergration.domain.user

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyRequestTestBuilder.aPartyRequest
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class UserQueryTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentExpenseRepository expenseRepository

    @Autowired
    PersistentPaymentRepository paymentRepository

    @Autowired
    PersistentPartyRequestRepository partyRequestRepository

    def "Should return correct user"() {
        given:
        authenticate()

        and:
        def client = aClient([
                email      : 'email@gmail.com',
                name       : 'Some name',
                bankAccount: '39012382903820983'
        ], userRepository)
        def aParty = aParty([owner: aClient(userRepository), participants: [client]], partyRepository)
        def aPartyRequest = aPartyRequest([party: aParty, user: client], partyRequestRepository)
        def anExpense = anExpense([party: aParty, user: client], expenseRepository)
        def aPayment = aPayment([user: client, expense: anExpense], paymentRepository)


        and:
        def signUpQuery = """
            getUser(id: "${client.id}"){
              id
              email
              name
              bankAccount
              userPartyRequests { id }
              userJoinedParties { id }
              userPayments { id }
              userExpenses { id }
            }
        """

        when:
        def response = postQuery(signUpQuery)

        then:
        response.id.toLong() == client.id
        response.email == 'email@gmail.com'
        response.name == 'Some name'
        response.bankAccount == '39012382903820983'
        response.userPartyRequests.size == 1
        response.userPartyRequests[0].id.toLong() == aPartyRequest.id
        response.userJoinedParties.size == 1
        response.userJoinedParties[0].id.toLong() == aParty.id
        response.userPayments.size == 1
        response.userPayments[0].id.toLong() == aPayment.id
        response.userExpenses.size == 1
        response.userExpenses[0].id.toLong() == anExpense.id
    }

    def "Should return correct users friends"() {
        given:
        authenticate()

        and:
        def client1 = aClient([friendOf: [baseUser]], userRepository)
        def client2 = aClient([friendOf: [baseUser]], userRepository)
        def client3 = aClient([friendOf: [baseUser]], userRepository)


        and:
        def findUsersFriendsQuery = """findUsersFriends(userId: "${baseUser.id}"){ id }"""

        when:
        def response = postQuery(findUsersFriendsQuery)

        then:
        response.size() == 3
        response.any { it.id.toLong() == client1.id }
        response.any { it.id.toLong() == client2.id }
        response.any { it.id.toLong() == client3.id }
    }

    def "Should return correct users inverse friends"() {
        given:
        authenticate()

        and:
        def client1 = aClient([friendOf: [baseUser]], userRepository)
        def client2 = aClient([friendOf: [baseUser]], userRepository)
        def client3 = aClient([friendOf: [baseUser]], userRepository)

        and:
        def findUsersFriendsQuery = { String id ->
            """
            findUsersFriends(userId: "${id}"){ id }
        """
        }

        when:
        def client1Response = postQuery(findUsersFriendsQuery(client1.id.toString()))
        def client2Response = postQuery(findUsersFriendsQuery(client2.id.toString()))
        def client3Response = postQuery(findUsersFriendsQuery(client3.id.toString()))

        then:
        client1Response.size() == 1
        client2Response.size() == 1
        client3Response.size() == 1
        client1Response[0].id.toLong() == baseUser.id
        client2Response[0].id.toLong() == baseUser.id
        client3Response[0].id.toLong() == baseUser.id
    }
}
