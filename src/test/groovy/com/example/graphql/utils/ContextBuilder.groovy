//package com.example.graphql.utils
//
//import com.example.graphql.configuration.context.AppGraphQLContext
//import org.springframework.http.server.reactive.ServerHttpRequest
//import org.springframework.http.server.reactive.ServerHttpResponse
//import org.springframework.http.server.reactive.ServletServerHttpResponse
//
//import static com.example.graphql.utils.VerifyingBuilder.verifyPropertyNames
//
//class ContextBuilder {
//
//    private static def defaults = [
//            authenticated: true,
//            subject      : '1',
//    ]
//
//
//    private UserTestBuilder() {}
//
//    static AppGraphQLContext defaultContext(Map args) {
//        verifyPropertyNames(defaults, args)
//
//        def allArgs = defaults + args
//        ServerHttpRequest request = Mock()
//        ServerHttpResponse response = Mock()
//
//        return new AppGraphQLContext(
//                allArgs.authenticated as Boolean,
//                allArgs.subject as String,
//                request,
//                response,
//        )
//    }
//}
