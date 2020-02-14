package intergration.domain.auth

import intergration.BaseIntegrationSpec
import intergration.utils.CookiesUtils
import intergration.utils.JWTUtils
import intergration.utils.SecurityConstants
import spock.lang.Unroll

class AuthMutationTest extends BaseIntegrationSpec {

    def "Should return store correct jwt and store refresh token in cookie token after correct sign up"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "fdak"}) { id }'

        when:
        String newUserId = postMutation(signUpMutation, "signUp").id

        then:
        def accessTokenSubject = JWTUtils.getJWTTokenSubject(CookiesUtils.getCookieValue(SecurityConstants.ACCESS_TOKEN, restClient))
        def refreshTokenSubject = JWTUtils.getJWTTokenSubject(CookiesUtils.getCookieValue(SecurityConstants.REFRESH_TOKEN, restClient))

        accessTokenSubject == newUserId
        refreshTokenSubject == newUserId
    }

    @Unroll
    def "Should return store correct jwt and store refresh token in cookie token after correct [#loginCorrect] login"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "correct password"}) { id }'

        when:
        postMutation(signUpMutation, "signUp")

        and:
        CookiesUtils.removeCookie(SecurityConstants.ACCESS_TOKEN, restClient)
        CookiesUtils.removeCookie(SecurityConstants.REFRESH_TOKEN, restClient)

        and:
        def userResponse = postMutation(loginMutation, "logIn")

        then:
        def accessToken = JWTUtils.getJWTToken(CookiesUtils.getCookieValue(SecurityConstants.ACCESS_TOKEN, restClient))
        def refreshToken = JWTUtils.getJWTToken(CookiesUtils.getCookieValue(SecurityConstants.REFRESH_TOKEN, restClient))

        if (loginCorrect) {
            assert accessToken.subject == userResponse.id
            assert refreshToken.subject == userResponse.id
        } else {
            assert accessToken == null
            assert refreshToken == null
        }

        where:
        loginMutation                                                               | loginCorrect
        'logIn(input: {email: "a@gmail.com", password: "correct password"}) { id }' | true
        'logIn(input: {email: "a@gmail.com", password: "wrong password"}) { id }'   | false
    }

    def "Should allow user to use mutation when jwt is not present but refresh token is"() {
        given:
        def signUpMutation = 'signUp(input: {email: "a@gmail.com", password: "correct password"}) { id }'
        def authenticationNeededQuery = { String id -> (' getUser(id: "' + id + '"){ id }') }

        when:
        String userId = postMutation(signUpMutation, "signUp").id

        and:
        String expiredToken = JWTUtils.expireToken(CookiesUtils.getCookieValue(SecurityConstants.ACCESS_TOKEN, restClient))

        and:
        CookiesUtils.setCookieValue(SecurityConstants.ACCESS_TOKEN, expiredToken, restClient)

        and:
        def userResponse = postQuery(authenticationNeededQuery(userId), "getUser")

        then:
        def accessToken = JWTUtils.getJWTToken(CookiesUtils.getCookieValue(SecurityConstants.ACCESS_TOKEN, restClient))
        def refreshToken = JWTUtils.getJWTToken(CookiesUtils.getCookieValue(SecurityConstants.REFRESH_TOKEN, restClient))

        userResponse.id == userId
        accessToken.subject == userResponse.id
        refreshToken.subject == userResponse.id
    }
}
