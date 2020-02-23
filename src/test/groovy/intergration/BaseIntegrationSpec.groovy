package intergration

import com.example.graphql.GraphqlApplication
import com.example.graphql.adapters.pgsql.user.PersistentUserRepository
import com.example.graphql.configuration.security.JWTClient
import com.example.graphql.domain.user.PersistentUser
import groovy.json.JsonBuilder
import groovyx.net.http.RESTClient
import intergration.utils.builders.PersistentUserTestBuilder
import org.apache.groovy.json.internal.LazyMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
@SpringBootTest(classes = [GraphqlApplication],
        properties = "application.environment=integration",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseIntegrationSpec extends Specification {
    private Logger log = LoggerFactory.getLogger(BaseIntegrationSpec.getClass())
    protected String baseUserEmail = "a@gmail.com"
    protected String baseUserPassword = "Password"
    protected Long baseUserId
    protected PersistentUser baseUser

    @Value('${local.server.port}')
    protected int port

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    JWTClient jwtClient

    @Autowired
    PersistentUserRepository userRepository

    RESTClient restClient

    def setup() {
        cleanupTables()
        restClient = new RESTClient("http://localhost:$port", "application/json")
        restClient.setHeaders([
                "Content-Type": "application/graphql",
                "Accept"      : "application/json"
        ])
        restClient.handler.failure = restClient.handler.success
    }

    protected def authenticate(String email = "a@gmail.com") {
        baseUser = userRepository.save(PersistentUserTestBuilder.defaultPersistentUser([email: email]))

        baseUserId = baseUser.id

        setHeaders(["Authorization": "Bearer " + jwtClient.createJWTToken(baseUserId.toString())])
    }

    protected def postQuery(String query, String queryName = null, Boolean errorExpected = false) {
        def queryString = "query { ${query} }"
        def name = queryName != null ? queryName : query.tokenize('(').first().trim()

        return postToGraphQL(queryString, name, errorExpected)
    }

    protected def postMutation(String mutation, String mutationName = null, Boolean errorExpected = false) {
        def mutationString = "mutation { ${mutation} }"
        def name = mutationName != null ? mutationName : mutation.tokenize('(').first().trim()

        return postToGraphQL(mutationString, name, errorExpected)
    }

    private def postToGraphQL(String query, String queryName, Boolean errorExpected = false) {
        def responseData = restClient.post([
                path: "/graphql",
                body: query
        ]).responseData

        if (responseData.containsKey('status') && responseData.status == 500) {
            log.error(new JsonBuilder(responseData).toPrettyString())
        }

        if (errorExpected) {
            return responseData.errors
        }
        if ((responseData as LazyMap).containsKey('errors')) {
            log.error(new JsonBuilder(responseData.errors).toPrettyString())
        }

        return responseData.data[queryName]
    }

    protected setHeaders(Map<String, String> headers) {
        restClient.setHeaders([
                "Content-Type": "application/graphql",
                "Accept"      : "application/json"
        ] + headers)
    }

    def cleanup() {
        cleanupTables()
    }

    private def cleanupTables() {
        jdbcTemplate.execute("""
            TRUNCATE TABLE 
                party_user,
                friends,
                bulk_payments,
                expenses,
              message_groups,
              messagegroup_user ,
              messages ,
              parties ,
              party_requests ,
              payments,
              users 
            """)
    }
}
