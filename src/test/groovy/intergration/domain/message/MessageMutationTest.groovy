package intergration.domain.message

import com.example.graphql.adapters.pgsql.expense.PersistentExpenseRepository
import com.example.graphql.adapters.pgsql.message.PersistentBulkPaymentMessageRepository
import com.example.graphql.adapters.pgsql.message.PersistentExpenseMessageRepository
import com.example.graphql.adapters.pgsql.message.PersistentPartyMessageRepository
import com.example.graphql.adapters.pgsql.message.PersistentPaymentMessageRepository
import com.example.graphql.adapters.pgsql.party.PersistentPartyRepository
import com.example.graphql.adapters.pgsql.partyrequest.PersistentPartyRequestRepository
import com.example.graphql.adapters.pgsql.payment.PersistentBulkPaymentRepository
import com.example.graphql.adapters.pgsql.payment.PersistentPaymentRepository
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.resolvers.message.MessageType
import intergration.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import static intergration.utils.AssertionUtils.assertUnauthorizedError
import static intergration.utils.builders.MessageTestBuilder.*
import static intergration.utils.builders.PersistentBulkPaymentTestBuilder.aBulkPayment
import static intergration.utils.builders.PersistentExpenseTestBuilder.anExpense
import static intergration.utils.builders.PersistentPartyTestBuilder.aParty
import static intergration.utils.builders.PersistentPaymentTestBuilder.aPayment
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

class MessageMutationTest extends BaseIntegrationSpec {

    @Autowired
    PersistentUserRepository userRepository

    @Autowired
    PersistentPartyRepository partyRepository

    @Autowired
    PersistentPaymentRepository paymentRepository

    @Autowired
    PersistentBulkPaymentRepository bulkPaymentRepository

    @Autowired
    PersistentExpenseRepository expenseRepository

    @Autowired
    PersistentPartyRequestRepository partyRequestRepository

    @Autowired
    PersistentPartyMessageRepository partyMessageRepository

    @Autowired
    PersistentPaymentMessageRepository paymentMessageRepository

    @Autowired
    PersistentBulkPaymentMessageRepository bulkPaymentMessageRepository

    @Autowired
    PersistentExpenseMessageRepository expenseMessageRepository

    private static def createMessageMutation(Long entityId, MessageType type, String text) {
        return """
            createMessage(
                newMessageInput: {
                        text: "${text}"
                        entityId: ${entityId}
                        messageType: ${type}
                }
            ) { id }
        """
    }

    private static def removeMessageMutation(Long messageId, MessageType type) {
        return """removeMessage(messageId: ${messageId}, messageType: ${type})"""
    }

    private def createEntity(MessageType messageType, Boolean baseUserAsParticipant = true) {
        def user = baseUserAsParticipant ? baseUser : aClient(userRepository)

        switch (messageType) {
            case MessageType.PARTY:
                return aParty([owner: user], partyRepository)
            case MessageType.PAYMENT:
                return aPayment([expense: anExpense([user: user], expenseRepository), user: user], paymentRepository)
            case MessageType.BULK_PAYMENT:
                return aBulkPayment([payer: user, receiver: aClient(userRepository)], bulkPaymentRepository)
            case MessageType.EXPENSE:
                return anExpense([user: user], expenseRepository)
        }
    }

    private def createMessage(MessageType messageType, Boolean baseUserAsParticipant = true) {
        def user = baseUserAsParticipant ? baseUser : aClient(userRepository)

        switch (messageType) {
            case MessageType.PARTY:
                return aPartyMessage([user: user, party: createEntity(messageType)], partyMessageRepository)
            case MessageType.PAYMENT:
                return aPaymentMessage([user: user, payment: createEntity(messageType)], paymentMessageRepository)
            case MessageType.BULK_PAYMENT:
                return aBulkPaymentMessage([user: user, bulkPayment: createEntity(messageType)], bulkPaymentMessageRepository)
            case MessageType.EXPENSE:
                return aExpenseMessage([user: user, expense: createEntity(messageType)], expenseMessageRepository)
        }
    }

    @Unroll
    def "Should add a new party message"() {
        given:
        authenticate()

        and:
        def entity = createEntity(messageType)

        when:
        def messageId = postMutation(createMessageMutation(entity.id, messageType, 'test message')).id

        and:
        def actualMessage = jdbcTemplate.queryForMap("SELECT * from ${table} WHERE id = ${messageId} ")

        then:
        actualMessage.text == 'test message'
        actualMessage.user_id.toLong() == baseUser.id
        actualMessage.party_id.toLong() == party.id

        where:
        messageType              | table                   | entityIdField
        MessageType.PARTY        | "party_messages"        | "party_id"
        MessageType.PAYMENT      | "payment_messages"      | "payment_id"
        MessageType.BULK_PAYMENT | "bulk_payment_messages" | "bulk_payment_id"
        MessageType.EXPENSE      | "expense_messages"      | "expense_id"
    }

    @Unroll
    def "Should not add a new message when user is not a participant"() {
        given:
        authenticate()

        and:
        def entity = createEntity(messageType, false)

        when:
        def response = postMutation(createMessageMutation(entity.id, messageType, 'test message'))

        and:
        def actualMessage = jdbcTemplate.queryForMap("SELECT * from ${table} WHERE id = ${entity.id} ")

        then:
        actualMessage.size() == 0
        assertUnauthorizedError(response)

        where:
        messageType              | table                   | entityIdField
        MessageType.PARTY        | "party_messages"        | "party_id"
        MessageType.PAYMENT      | "payment_messages"      | "payment_id"
        MessageType.BULK_PAYMENT | "bulk_payment_messages" | "bulk_payment_id"
        MessageType.EXPENSE      | "expense_messages"      | "expense_id"
    }

    @Unroll
    def "Should remove a message"() {
        given:
        authenticate()

        and:
        def message = createMessage(messageType)

        when:
        postMutation(removeMessageMutation(message.id, messageType))

        and:
        def actualMessage = jdbcTemplate.queryForMap("SELECT * from ${table} WHERE id = ${message.id} ")

        then:
        actualMessage.size() == 0

        where:
        messageType              | table
        MessageType.PARTY        | "party_messages"
        MessageType.PAYMENT      | "payment_messages"
        MessageType.BULK_PAYMENT | "bulk_payment_messages"
        MessageType.EXPENSE      | "expense_messages"
    }

    def "Should not remove a message when user is not a messageOwner"() {
        given:
        authenticate()

        and:
        def message = createMessage(messageType, false)

        when:
        def response = postMutation(removeMessageMutation(message.id, messageType), null, true)

        and:
        def actualMessage = jdbcTemplate.queryForMap("SELECT * from ${table} WHERE id = ${message.id} ")

        then:
        actualMessage.size() == 1
        assertUnauthorizedError(response)

        where:
        messageType              | table
        MessageType.PARTY        | "party_messages"
        MessageType.PAYMENT      | "payment_messages"
        MessageType.BULK_PAYMENT | "bulk_payment_messages"
        MessageType.EXPENSE      | "expense_messages"
    }
}
