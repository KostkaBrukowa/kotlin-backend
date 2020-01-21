package com.example.graphql.configuration.security

import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse

fun ServerHttpRequest.isCookiePresent(name: String): Boolean = this.cookies.getFirst(name) != null

fun ServerHttpRequest.getCookieValue(name: String): String = this.cookies.getFirst(name)?.value ?: ""

fun ServerHttpRequest.isHeaderPresent(name: String): Boolean = !this.headers.getFirst(name).isNullOrBlank()

fun ServerHttpRequest.getHeaderValue(name: String): String = this.headers.getFirst(name) ?: ""

fun ServerHttpResponse.removeCookie(name: String, path: String = "/"): Unit =
        this.addCookie(
                ResponseCookie.from(name, "").apply {
                    this.path(path)
                    this.maxAge(0)
                }.build()
        )

fun ServerHttpResponse.addHeaders(name: String, values: List<String>) = values.forEach {
    this.headers.add(name, it)
}

