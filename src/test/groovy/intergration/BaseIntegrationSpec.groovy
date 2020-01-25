package intergration

import com.example.graphql.GraphqlApplication
import groovyx.net.http.RESTClient
import intergration.utils.CookiesUtils
import intergration.utils.SecurityConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Ignore
import spock.lang.Specification

import java.time.ZoneId

@Ignore
@SpringBootTest(classes = [GraphqlApplication],
        properties = "application.environment=integration",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseIntegrationSpec extends Specification {
    protected String baseUserEmail = "a@gmail.com"
    protected String baseUserPassword = "Password"
    protected String baseUserId

    @Value('${local.server.port}')
    protected int port

    @Autowired
    JdbcTemplate jdbcTemplate

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
    protected def authenticate(String email) {
        def signUpMutation = "signUp(input: {email: \"${email ?: baseUserEmail}\", password: \"${baseUserPassword}\" }) { id }"

        baseUserId = postMutation(signUpMutation, "signUp").id

        String newUserJWTToken = CookiesUtils.getCookieValue(SecurityConstants.ACCESS_TOKEN, restClient)

        setHeaders(["Authorization": "Bearer " + newUserJWTToken])
    }

    protected def postQuery(String query, String queryName, Boolean errorExpected = false) {
        def queryString = "query { ${query} }"

        return postToGraphQL(queryString, queryName, errorExpected)
    }

    protected def postMutation(String mutation, String mutationName, Boolean errorExpected = false) {
        def mutationString ="mutation { ${mutation} }"

        return postToGraphQL(mutationString, mutationName, errorExpected)
    }

    private def postToGraphQL(String query, String queryName, Boolean errorExpected = false) {
        def responseData = restClient.post([
                path: "/graphql",
                body: query
        ]).responseData

        if(errorExpected) {
            return responseData.errors
        }

        return responseData.data[queryName]
    }

    protected setHeaders(Map<String, String> headers) {
        restClient.setHeaders([
                "Content-Type" : "application/graphql",
                "Accept"       : "application/json"
        ] + headers)
    }

    def cleanup() {
        cleanupTables()
    }

    private def cleanupTables() {
        jdbcTemplate.execute("""
            TRUNCATE TABLE expenses,
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
