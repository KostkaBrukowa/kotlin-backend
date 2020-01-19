package intergration.domain.user

import com.auth0.jwt.JWT
import intergration.BaseIntegrationSpec
import spock.lang.Unroll

class UserMutationTest extends BaseIntegrationSpec {

    def decodeJWT(String jwt) {
        return JWT.decode(jwt).subject
    }

    @Unroll
    def "Should send different reponse when we set authentication header (#setAuthorizationHeader) or not"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "fdak"})'

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
        def newUserJWTToken = postMutation(signUpMutation, "signUp")

        and:
        if (setAuthorizationHeader)
            setHeaders(["Authorization": "Bearer " + newUserJWTToken])

        and:
        def getUserResponse = postQuery(signUpQuery(decodeJWT(newUserJWTToken)), "getUser")

        then:
        !setAuthorizationHeader || getUserResponse.id == decodeJWT(newUserJWTToken)
        getUserResponse?.email == email

        where:
        setAuthorizationHeader | email
        true                   | "a@gmail.com"
        false                  | null
    }
}
