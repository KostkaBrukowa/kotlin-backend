package intergration.utils

import groovyx.net.http.RESTClient

class CookiesUtils {
    static String getCookieValue(String cookieName, RESTClient restClient) {
        return restClient.client.cookieStore.cookies.find { it.name == cookieName }.value
    }

    static String setCookieValue(String cookieName, String cookieValue, RESTClient restClient) {
        return restClient.client.cookieStore.cookies.find { it.name == cookieName }.value = cookieValue
    }

    static def removeCookie(String cookieName, RESTClient restClient) {
        restClient.client.cookieStore.getCookies().find { it.name == cookieName }.setValue("")
    }
}
