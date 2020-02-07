package intergration.domain.user


import intergration.BaseIntegrationSpec
import intergration.utils.CookiesUtils
import intergration.utils.JWTUtils
import intergration.utils.SecurityConstants
import spock.lang.Unroll

class UserMutationTest extends BaseIntegrationSpec {

    @Unroll
    def "Should return a user when user is authenticated"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "fdak"}) { id }'

        and:
        def signUpQuery = { String id ->
            """
            getUser(id: "${id}"){
              id
              email
            }
        """
        }

        when:
        postMutation(signUpMutation, "signUp")

        and:
        String newUserJWTToken = CookiesUtils.getCookieValue(SecurityConstants.ACCESS_TOKEN, restClient)

        and:
        def getUserResponse = postQuery(signUpQuery(JWTUtils.getJWTTokenSubject(newUserJWTToken)), "getUser")

        then:
        getUserResponse.id == JWTUtils.getJWTTokenSubject(newUserJWTToken)
        getUserResponse.email == "a@gmail.com"
    }

    def "Should sent error response when user is not authenticated"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "fdak"}) { id }'

        and:
        def signUpQuery = { String id ->
            """
            getUser(id: "${id}"){
              id
              email
            }
        """
        }

        when:
        postMutation(signUpMutation, "signUp")

        and:
        String newUserJWTToken = CookiesUtils.getCookieValue(SecurityConstants.ACCESS_TOKEN, restClient)

        and:
        CookiesUtils.removeCookie(SecurityConstants.ACCESS_TOKEN, restClient)
        CookiesUtils.removeCookie(SecurityConstants.REFRESH_TOKEN, restClient)

        and:
        def getUserResponse = postQuery(signUpQuery(JWTUtils.getJWTTokenSubject(newUserJWTToken)), "getUser", true)

        then:
        getUserResponse[0].errorType == "ExecutionAborted"
    }
}
