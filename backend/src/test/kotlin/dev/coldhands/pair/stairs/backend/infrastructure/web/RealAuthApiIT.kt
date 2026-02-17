package dev.coldhands.pair.stairs.backend.infrastructure.web

import io.kotest.matchers.nulls.shouldNotBeNull
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.filter.ClientFilters
import org.http4k.filter.cookie.BasicCookieStorage
import org.http4k.lens.*
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class RealAuthApiIT {

    @Test
    fun `can handle token authentication for api`() = testContext {
        val server = underTest.asServer(SunHttp(8080)).start()

        val appBaseUri = Uri.of("http://localhost:${server.port()}")

        val client = ClientFilters.SetBaseUriFrom(appBaseUri)
            .then(JavaHttpClient())

        val unauthedResponse = client(Request(Method.GET, "/api/v1/teams")
            .accept(APPLICATION_JSON))

        println(unauthedResponse)
    }

    @Test
    @Disabled
    // todo delete me
    fun `manual stuff`() = testContext {
        val server = underTest.asServer(SunHttp(8080)).start()

        val appBaseUri = Uri.of("http://localhost:${server.port()}")
        val cookieStorage = BasicCookieStorage()

        val client = ClientFilters.FollowRedirects()
            .then(ClientFilters.Cookies(storage = cookieStorage))
            .then(JavaHttpClient())

        val unauthedResponse = client(Request(Method.GET, appBaseUri.appendToPath("/")))

        println(unauthedResponse)

        val actionRegex = Regex("<form method=\"post\".*action=\"(.+)\">")
        val action = actionRegex.find(unauthedResponse.bodyString())
            ?.groups?.get(1)?.value
            .shouldNotBeNull()

        val login = "admin@example.com"
        val password = "password"
        println(action)

        val oAuthBaseUri = Uri.of("http://localhost:5556")
        val webFormLens = Body.webForm(
            Validator.Ignore,
            FormField.required("login"),
            FormField.required("password"),
        )
            .toLens()

        val loginUri = oAuthBaseUri.appendToPath(action)
        println(loginUri)

        val loginRequest = webFormLens(
            WebForm(
                mapOf(
                    "login" to listOf(login),
                    "password" to listOf(password),
                )
            ), Request(Method.POST, loginUri)
        )

        val loginResponse = client(loginRequest)

        println(loginResponse)
    }

}