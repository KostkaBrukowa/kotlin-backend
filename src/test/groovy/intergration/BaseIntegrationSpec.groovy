package intergration

import com.example.graphql.GraphqlApplication
import groovyx.net.http.RESTClient
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Ignore
import spock.lang.Specification

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Ignore
@SpringBootTest(classes = [GraphqlApplication],
        properties = "application.environment=integration",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseIntegrationSpec extends Specification {
    private def DEFAULT_TIMEZONE_NAME = "Europe/Warsaw"
    private def DEFAULT_TIMEZONE = ZoneId.of(DEFAULT_TIMEZONE_NAME)
    def DATA_JSON_PATH = "data" as String
    def ERRORS_JSON_PATH = "errors"
    def EXTENSIONS_JSON_PATH = "extensions"
    def GRAPHQL_ENDPOINT = "/graphql"
    def GRAPHQL_MEDIA_TYPE = new MediaType("application", "graphql")
    def SUBSCRIPTION_ENDPOINT = "/subscriptions"

    @Value('${local.server.port}')
    protected int port

    @Autowired
    JdbcTemplate jdbcTemplate

//    @Autowired
//    DateTimeProvider dateTimeProvider

    RESTClient restClient

    def setup() {
        cleanupTables()
        restClient = new RESTClient("http://localhost:$port", "application/json")
        restClient.setHeaders([
                "Content-Type": "application/graphql",
                "Accept"      : "application/json"
        ])
        restClient.handler.failure = restClient.handler.success
//        mockDateTimeTo("2018-06-01T12:00:00+02:00")
    }

    protected static createRegularUserHeaders(userId) {
        return [
                "X-PPC-User-Id"   : userId,
                "X-PPC-User-Roles": "REGULAR"
        ]
    }

    protected static createAdminUserHeaders(userId, sudoUserId = null) {
        def headers = [
                "X-PPC-User-Id"   : userId,
                "X-PPC-User-Roles": "ADMIN"
        ]
        if (sudoUserId != null) {
            headers += ["X-PPC-Sudo-User-Id": sudoUserId]
        }
        return headers
    }

    protected def mockDateTimeTo(String dateTime) {
        mockDateTimeTo(ZonedDateTime.parse(dateTime).withZoneSameInstant(DEFAULT_TIMEZONE))
    }

    protected def mockDateTo(String date) {
        mockDateTimeTo(LocalDate.parse(date).atStartOfDay(DEFAULT_TIMEZONE))
    }

    protected def mockDateTimeTo(ZonedDateTime dateTime) {
        Mockito.when(dateTimeProvider.currentDateTime()).thenReturn(dateTime)
        Mockito.when(dateTimeProvider.currentDate()).thenReturn(dateTime.toLocalDate())
    }

    private def postToGraphQL(String query, String queryName, Boolean errorExpected = false) {
        def responseData = restClient.post([
                path: "/graphql",
                body: query
        ]).responseData

        if(errorExpected) {
            return responseData.error
        }

        return responseData.data[queryName]
    }

    protected def postQuery(String query, String queryName, Boolean errorExpected = false) {
        def queryString = "query { ${query} }"

        return postToGraphQL(queryString, queryName, errorExpected)
    }

    protected def postMutation(String mutation, String mutationName, Boolean errorExpected = false) {
        def mutationString ="mutation { ${mutation} }"

        return postToGraphQL(mutationString, mutationName, errorExpected)
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
