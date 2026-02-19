package dev.coldhands.pair.stairs.backend.infrastructure.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.annotation.JsonProperty
import io.kotest.matchers.nulls.shouldNotBeNull
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.ContentType.Companion.APPLICATION_FORM_URLENCODED
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.body.form
import org.http4k.core.cookie.cookie
import org.http4k.filter.ClientFilters
import org.http4k.filter.cookie.BasicCookieStorage
import org.http4k.format.Jackson.auto
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.*
import org.http4k.security.AccessToken
import org.http4k.security.CredentialsProvider
import org.http4k.security.ExpiringCredentials
import org.http4k.security.Refreshing
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaInstant

class ApiAuthIT {

    @Test
    fun `api request without authorization should 401`() = testContext {
        underTest.asServer(SunHttp(8080)).use { server ->
            server.start()
            val appBaseUri = Uri.of("http://localhost:${server.port()}")

            val client = ClientFilters.SetBaseUriFrom(appBaseUri)
                .then(JavaHttpClient())

            val response = client(
                Request(Method.GET, "/api/v1/teams")
                    .accept(APPLICATION_JSON)
            )

            response shouldHaveStatus UNAUTHORIZED
        }
    }

    @Test
    @Disabled
    fun `unprotected route without authorization should return successfully`() = testContext {
        TODO()
    }

    @Test
    fun `api route with bearer token should return successfully`() = testContext {
        underTest.asServer(SunHttp(8080)).use { server ->
            server.start()

            val appBaseUri = Uri.of("http://localhost:${server.port()}")

            val appClient = ClientFilters.SetBaseUriFrom(appBaseUri)
                .then(dexAuthenticatedClient())

            val response = appClient(
                Request(Method.GET, "/api/v1/teams")
                    .accept(APPLICATION_JSON)
            )

            response shouldHaveStatus OK
        }
    }

    @Test
    fun `api route with cookie should return successfully`() = testContext {
        underTest.asServer(SunHttp(8080)).use { server ->
            server.start()

            val appBaseUri = Uri.of("http://localhost:${server.port()}")
            val cookieValue = UUID.randomUUID().toString()

            val client = ClientFilters.SetBaseUriFrom(appBaseUri)
                .then(Filter { next ->
                    { request ->
                        next(request.cookie("pair-stairsAuth", cookieValue))
                    }
                })
                .then(JavaHttpClient())

            cookieTokenStore[cookieValue] = AccessToken(value = generateSignedJwt())

            val response = client(
                Request(Method.GET, "/api/v1/teams")
                    .accept(APPLICATION_JSON)
            )

            response shouldHaveStatus OK
        }
    }

    private fun generateSignedJwt(): String {
        val algorithm = with(KeyPairGenerator.getInstance("RSA")) {
            initialize(4096)
            val keyPair = generateKeyPair()
            Algorithm.RSA256(keyPair.private as RSAPrivateKey)
        }
        val validJwt = JWT.create()
            .withSubject("some-subject")
            .sign(algorithm)
        return validJwt
    }

    private fun dexAuthenticatedClient(): HttpHandler {
        val authBaseUri = Uri.of("http://localhost:5556")
        val authClient = ClientFilters.SetBaseUriFrom(authBaseUri)
            .then(ClientFilters.BasicAuth("pair-stairs", "ZXhhbXBsZS1hcHAtc2VjcmV0"))
            .then(JavaHttpClient())

        data class TokenResponse(
            @JsonProperty("access_token") val accessToken: String,
            @JsonProperty("expires_in") val expiresIn: Long,
        )

        val tokenResponseLens = Body.auto<TokenResponse>().toLens()

        val appClient = ClientFilters.BearerAuth(CredentialsProvider.Refreshing(refreshFn = {
            val authResponse = authClient(
                Request(Method.POST, "/dex/token")
                    .contentType(APPLICATION_FORM_URLENCODED)
                    .form(
                        "grant_type" to "password",
                        "username" to "admin@example.com",
                        "password" to "password",
                        "scope" to "openid profile email",
                    )
            )
            authResponse shouldHaveStatus OK
            val tokenResponse = tokenResponseLens(authResponse)
            ExpiringCredentials(
                credentials = tokenResponse.accessToken,
                expiry = Clock.System.now().plus(tokenResponse.expiresIn.milliseconds).toJavaInstant()
            )
        }))
            .then(JavaHttpClient())

        return appClient
    }

}