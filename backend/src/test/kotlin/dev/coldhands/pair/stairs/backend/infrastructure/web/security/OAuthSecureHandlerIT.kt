package dev.coldhands.pair.stairs.backend.infrastructure.web.security

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
import org.http4k.routing.bind
import org.http4k.security.AccessToken
import org.http4k.security.CredentialsProvider
import org.http4k.security.ExpiringCredentials
import org.http4k.security.Refreshing
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaInstant

class OAuthSecureHandlerIT : OAuthSecureHandlerCdc {
    private val appBaseUri = Uri.of("http://localhost:8080")
    private val oAuthSettings = OAuthSettings(
        issuerUri = Uri.of("http://localhost:5556/dex"),
        jwkUri = Uri.of("http://localhost:5556/dex/keys"),
        callbackUri = appBaseUri.appendToPath("/login/oauth2/code/oauth"),
        audience = "pair-stairs",
        clientId = "pair-stairs",
        clientSecret = "ZXhhbXBsZS1hcHAtc2VjcmV0",
        loginTokenCookieValidity = 1.minutes
    )
    private val cookieTokenStore = mutableMapOf<String, AccessToken>()
    private val underTest = OAuthSecureHandler(
        routes = OAuthSecureHandler.Routes(
            apiRoutes = "/api/test" bind Method.GET to { Response(OK) },
            authFilteredRoutes = "/api/other" bind Method.GET to { Response(OK) },
        ),
        oAuthSettings = oAuthSettings,
        oAuthClient = JavaHttpClient(),
        clock = Clock.System,
        cookieTokenStore = cookieTokenStore,
    )

    override fun withHttpClient(block: (client: HttpHandler, cookieTokenStore: MutableMap<String, AccessToken>, oAuthSettings: OAuthSettings) -> Unit) {
        underTest.asServer(SunHttp(appBaseUri.port!!)).use { server ->
            server.start()

            val client = ClientFilters.SetBaseUriFrom(appBaseUri)
                .then(JavaHttpClient())

            block(client, cookieTokenStore, oAuthSettings)
        }
    }

    override fun withBearerAuthHttpClient(block: (client: HttpHandler) -> Unit) {
        underTest.asServer(SunHttp(appBaseUri.port!!)).use { server ->
            server.start()
            val appClient = ClientFilters.SetBaseUriFrom(appBaseUri)
                .then(dexAuthenticatedClient(oAuthSettings))

            block(appClient)
        }
    }

    override fun retrieveValidJwt(oAuthSettings: OAuthSettings): String =
        dexClient(oAuthSettings).getJwt().accessToken

    private fun dexAuthenticatedClient(oAuthSettings: OAuthSettings): HttpHandler {
        val dexClient = dexClient(oAuthSettings)

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

    private fun dexClient(oAuthSettings: OAuthSettings): HttpHandler {
        val authBaseUri = oAuthSettings.issuerUri.copy(path = "")
        val authClient = ClientFilters.SetBaseUriFrom(authBaseUri)
            .then(ClientFilters.BasicAuth(oAuthSettings.clientId, oAuthSettings.clientSecret))
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