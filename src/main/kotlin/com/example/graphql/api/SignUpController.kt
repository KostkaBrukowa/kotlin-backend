package com.example.graphql.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class HelloController {

    @PostMapping("/sign-up")
    fun signUp() {}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserCredentials(
        @JsonProperty("email")
        val email: String,

        @JsonProperty("password")
        val password: String
)
