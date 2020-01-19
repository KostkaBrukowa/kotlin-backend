package intergration.domain.user

import com.auth0.jwt.JWT
import intergration.BaseIntegrationSpec

class UserMutationTest extends BaseIntegrationSpec {

    def decodeJWT(String jwt) {
        return JWT.decode(jwt).subject
    }

    def "test"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "fdak"})'

        and:
        def signUpQuery = { String id ->
            """
            getUser(id: "${id}"){
              id
              name
              partyRequests {
                id
              }
            }
        """
        }

        when:
        def newUserJWTToken = postMutation(signUpMutation, "signUp")

        and:
        def getUserResponse = postQuery(signUpQuery(decodeJWT(newUserJWTToken)), "getUser")

        then:
        getUserResponse.id == decodeJWT(newUserJWTToken)
        getUserResponse.name == null
        getUserResponse.partyRequests.size() == 0
    }
}
