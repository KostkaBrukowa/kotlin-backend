package intergration.domain.auth

import intergration.BaseIntegrationSpec
import intergration.utils.CookiesUtils
import intergration.utils.JWTUtils
import intergration.utils.SecurityConstants
import spock.lang.Unroll

class AuthMutationTest extends BaseIntegrationSpec {

    def "Should return correct jwt and store refresh token in cookie token after correct sign up"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "fdak"}) '

        when:
        String accessToken = postMutation(signUpMutation, "signUp")

        then:
        def accessTokenSubject = JWTUtils.getJWTTokenSubject(accessToken)
        def refreshTokenSubject = JWTUtils.getJWTTokenSubject(CookiesUtils.getCookieValue(SecurityConstants.REFRESH_TOKEN, restClient))

        accessTokenSubject != null
        refreshTokenSubject != null
    }

    @Unroll
    def "Should return store correct jwt and store refresh token in cookie token after correct [#loginCorrect] login"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "correct password"}) '

        when:
        postMutation(signUpMutation, "signUp")

        and:
        CookiesUtils.removeCookie(SecurityConstants.REFRESH_TOKEN, restClient)

        and:
        String accessTokenResponse = postMutation(loginMutation, "logIn")

        then:
        def accessToken = JWTUtils.getJWTToken(accessTokenResponse)
        def refreshToken = JWTUtils.getJWTToken(CookiesUtils.getCookieValue(SecurityConstants.REFRESH_TOKEN, restClient))

        if (loginCorrect) {
            assert accessToken.subject != null
            assert refreshToken.subject != null
        } else {
            assert accessToken == null
            assert refreshToken == null
        }

        where:
        loginMutation                                                         | loginCorrect
        'logIn(input: {email: "a@gmail.com", password: "correct password"}) ' | true
        'logIn(input: {email: "a@gmail.com", password: "wrong password"}) '   | false
    }

    def "Refresh token enpoint should work"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "correct password"}) '
        def refreshTokenMutation = 'refreshToken'

        when:
        postMutation(signUpMutation)

        and:
        def refreshToken = postMutation(refreshTokenMutation)

        then:
        refreshToken != null
    }

    def "Should reject request with expired token"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "correct password"}) '
        def authenticationNeededQuery = { String id -> (' getUser(id: "' + id + '"){ id }') }

        when:
        String accessTokenResponse = postMutation(signUpMutation)

        and:
        String expiredToken = JWTUtils.expireToken(accessTokenResponse)

        and:
        setHeaders(["Authorization": "Bearer " + expiredToken])

        and:
        def userId = JWTUtils.getJWTToken(accessTokenResponse).subject
        def userResponse = postQuery(authenticationNeededQuery(userId), null, true)

        then:
        userResponse[0].message.contains('Token authentication failed')
    }
}
