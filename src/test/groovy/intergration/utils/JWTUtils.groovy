package intergration.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.graphql.configuration.security.SecurityConstants

class JWTUtils {
    static String getJWTTokenSubject(String jwt) {
        return getJWTToken(jwt).subject
    }

    static DecodedJWT getJWTToken(String jwt) {
        try {
            return JWT.decode(jwt)
        }
        catch (ignored) {
            return null
        }
    }

    static String expireToken(String jwt) {
        def decodedToken = getJWTToken(jwt)

        return JWT.create()
                .withSubject(decodedToken.subject)
                .withExpiresAt(new Date(System.currentTimeMillis()-60*60*1000))
                .sign(Algorithm.HMAC512(SecurityConstants.SECRET.bytes))
    }
}
