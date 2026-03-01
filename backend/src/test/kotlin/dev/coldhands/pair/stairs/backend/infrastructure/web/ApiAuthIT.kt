package dev.coldhands.pair.stairs.backend.infrastructure.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.ContentType.Companion.APPLICATION_FORM_URLENCODED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.body.form
import org.http4k.filter.ClientFilters
import org.http4k.format.Jackson.auto
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.contentType
import org.http4k.security.CredentialsProvider
import org.http4k.security.ExpiringCredentials
import org.http4k.security.Refreshing
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaInstant

class ApiAuthIT : ApiAuthCdc {
    override fun withHttpClient(block: (client: HttpHandler, testContext: TestContext) -> Unit) {
        testContext {
            oauthClient = JavaHttpClient()
            underTest.asServer(SunHttp(8080)).use { server ->
                server.start()
                val appBaseUri = Uri.of("http://localhost:${server.port()}")

                val client = ClientFilters.SetBaseUriFrom(appBaseUri)
                    .then(JavaHttpClient())

                block(client, this)
            }
        }
    }

    override fun withBearerAuthHttpClient(block: (client: HttpHandler, testContext: TestContext) -> Unit) {
        testContext {
            oauthClient = JavaHttpClient()
            underTest.asServer(SunHttp(8080)).use { server ->
                server.start()
                val appBaseUri = Uri.of("http://localhost:${server.port()}")

            val appClient = ClientFilters.SetBaseUriFrom(appBaseUri)
                .then(dexAuthenticatedClient())

                block(appClient, this)
            }
        }
    }

    override fun retrieveValidJwt(): String {
        return dexClient().getJwt().accessToken
    }

    private fun dexAuthenticatedClient(): HttpHandler {
        val dexClient = dexClient()

        val appClient = ClientFilters.BearerAuth(CredentialsProvider.Refreshing(refreshFn = {
            val tokenResponse = dexClient.getJwt()
            ExpiringCredentials(
                credentials = tokenResponse.accessToken,
                expiry = Clock.System.now().plus(tokenResponse.expiresIn.milliseconds).toJavaInstant()
            )
        }))
            .then(JavaHttpClient())

        return appClient
    }

    private fun dexClient(): HttpHandler {
        val authBaseUri = Uri.of("http://localhost:5556")
        val authClient = ClientFilters.SetBaseUriFrom(authBaseUri)
            .then(ClientFilters.BasicAuth("pair-stairs", "ZXhhbXBsZS1hcHAtc2VjcmV0"))
            .then(JavaHttpClient())
        return authClient
    }

    private fun HttpHandler.getJwt(): TokenResponse {
        val tokenResponseLens = Body.auto<TokenResponse>().toLens()

        val authResponse = invoke(
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
        return tokenResponseLens(authResponse)
    }

    private data class TokenResponse(
        @JsonProperty("access_token") val accessToken: String,
        @JsonProperty("expires_in") val expiresIn: Long,
    )

}