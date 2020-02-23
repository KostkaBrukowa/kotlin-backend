package intergration.domain.user


import intergration.BaseIntegrationSpec
import intergration.utils.CookiesUtils
import intergration.utils.JWTUtils
import intergration.utils.SecurityConstants
import spock.lang.Unroll

import static intergration.utils.AssertionUtils.assertUnauthorizedError
import static intergration.utils.builders.PersistentUserTestBuilder.aClient

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
        postMutation(signUpMutation)

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

    def "Should add other client as a friend"() {
        given:
        authenticate()

        and:
        def client = aClient(userRepository)

        and:
        def addFriendMutation = """addFriend(userId: ${client.id})"""

        when:
        postMutation(addFriendMutation)

        and:
        def baseUserFriends = getBaseUserFriendsMap()

        then:
        baseUserFriends.size() == 1
        baseUser[0].user_id == baseUser.id
        baseUser[0].friend_id == client.id
    }


    def "Should remove client from friends list"() {
        given:
        authenticate()

        and:
        def client = aClient([friendOf: [baseUser]], userRepository)

        and:
        def removeFriendMutation = """removeFriend(userId: ${client.id})"""

        when:
        postMutation(removeFriendMutation)

        and:
        def baseUserFriends = getBaseUserFriendsMap()

        then:
        baseUserFriends.isEmpty()
    }

    def "Should not add a client as a friend when other user is already a friend"() {
        given:
        authenticate()

        and:
        def client = aClient([friendOf: [baseUser]], userRepository)

        and:
        def addFriendMutation = """addFriend(userId: ${client.id})"""

        when:
        def response = postMutation(addFriendMutation)

        and:
        def baseUserFriends = getBaseUserFriendsMap()

        then:
        baseUserFriends.size() == 1
        baseUser[0].user_id == baseUser.id
        baseUser[0].friend_id == client.id

        assertUnauthorizedError(response)
    }

    def "Should return an error after removing a friend that doesnt exist"() {
        given:
        authenticate()

        and:
        def friend = aClient([friendOf: [baseUser]], userRepository)
        def notAFriend = aClient(userRepository)

        and:
        def removeFriendMutation = """removeFriend(userId: ${notAFriend.id})"""

        when:
        def response = postMutation(removeFriendMutation)

        and:
        def baseUserFriends = getBaseUserFriendsMap()

        then:
        baseUserFriends.size() == 1
        baseUserFriends[0].friend_id == friend.id
        assert response[0].errorType == 'ValidationError'
    }

    private def getBaseUserFriendsMap() {
        return jdbcTemplate.queryForList("""
            SELECT * from friends WHERE user_id = ${baseUser.id} OR friend_id = ${baseUser.id}
        """)
    }
}
