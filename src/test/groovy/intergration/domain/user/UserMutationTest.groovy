package intergration.domain.user


import intergration.BaseIntegrationSpec
import intergration.utils.CookiesUtils
import intergration.utils.JWTUtils
import spock.lang.Unroll

class UserMutationTest extends BaseIntegrationSpec {
    def ACCESS_TOKEN = "xppctkn"
    def REFRESH_TOKEN = "xppcreftkn"

    @Unroll
    def "Should send different response when we remove authentication cookies (#clearAuthenticationCookie) or not"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "fdak"}) { id }'

        and:
        def signUpQuery = { String id ->
            """
            getUser(id: "${id}"){
              id
              email
              partyRequests {
                id
              }
            }
        """
        }

        when:
        postMutation(signUpMutation, "signUp")

        and:
        String newUserJWTToken = CookiesUtils.getCookieValue(ACCESS_TOKEN, restClient)

        and:
        if (clearAuthenticationCookie) {
            CookiesUtils.removeCookie(ACCESS_TOKEN, restClient)
            CookiesUtils.removeCookie(REFRESH_TOKEN, restClient)
//            String expiredToken = JWTUtils.expireToken(newUserJWTToken)
//
//            CookiesUtils.setCookieValue(ACCESS_TOKEN, expiredToken, restClient)
        }

        and:
        def getUserResponse = postQuery(signUpQuery(JWTUtils.getJWTTokenSubject(newUserJWTToken)), "getUser")

        then:
        clearAuthenticationCookie || getUserResponse.id == JWTUtils.getJWTTokenSubject(newUserJWTToken)
        getUserResponse?.email == email

        where:
        clearAuthenticationCookie | email
        false                     | "a@gmail.com"
        true                      | null
    }
}
