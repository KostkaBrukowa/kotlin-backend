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
        def friend = aClient([friendOf: [client]], userRepository)


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
              userFriends { id }
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
        response.userFriends.size == 1
        response.userFriends[0].id.toLong() == friend.id
    }


}
