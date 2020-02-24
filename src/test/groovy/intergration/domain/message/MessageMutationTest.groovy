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
            )
        """
    }

    private static def removeMessageMutation(Long messageId, MessageType type) {
        return """removeMessage(messageId: ${messageId}, messageType: ${type})"""
    }

    private def createEntity(MessageType messageType) {
        switch (messageType) {
            case MessageType.PARTY:
                return aParty([owner: baseUser], partyRepository)
            case MessageType.PAYMENT:
                return aPayment([expense: anExpense([user: baseUser], expenseRepository), user: baseUser], paymentRepository)
            case MessageType.BULK_PAYMENT:
                return aBulkPayment([payer: baseUser, receiver: aClient(userRepository)], bulkPaymentRepository)
            case MessageType.EXPENSE:
                return anExpense([user: baseUser], expenseRepository)
        }
    }

    private def createMessage(MessageType messageType, Object entity) {
        switch (messageType) {
            case MessageType.PARTY:
                return aPartyMessage([user: baseUser, party: createEntity(messageType)], partyMessageRepository)
            case MessageType.PAYMENT:
                return aPaymentMessage([user: baseUser, payment: createEntity(messageType)], paymentMessageRepository)
            case MessageType.BULK_PAYMENT:
                return aBulkPaymentMessage([user: baseUser, bulkPayment: createEntity(messageType)], bulkPaymentMessageRepository)
            case MessageType.EXPENSE:
                return aExpenseMessage([user: baseUser, expense: createEntity(messageType)], expenseMessageRepository)
        }
    }

    @Unroll
    def "Should add a new party message"() {
        given:
        authenticate()

        and:
        def entity = createEntity(messageType)

        when:
        postMutation(createMessageMutation(entity.id, messageType, 'test message'))

        and:
        def actualMessage = jdbcTemplate.queryForMap("SELECT * from ${table} WHERE id = ${entity.id} ")

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
    def "Should remove a message"() {
        given:
        authenticate()

        and:
        def entity = createEntity(messageType)
        def message = createMessage(messageType, entity)

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
}
