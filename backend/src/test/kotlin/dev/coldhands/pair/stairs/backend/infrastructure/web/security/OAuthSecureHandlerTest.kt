package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.http4k.core.*
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.filter.ClientFilters
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.bind
import org.http4k.security.AccessToken
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class OAuthSecureHandlerTest : OAuthSecureHandlerCdc {
    private val rsaKeyPair = generateRsaKeyPair()
    private val keyId = UUID.randomUUID().toString()
    private val oAuthSettings = OAuthSettings(
        issuerUri = Uri.of("/issuer"),
        jwkUri = Uri.of("/keys"),
        callbackUri = Uri.of("/login/oauth2/code/oauth"),
        audience = "pair-stairs",
        clientId = "some-client-id",
        clientSecret = "some-client-secret",
        loginTokenCookieValidity = 1.minutes
    )
    val fakeIdpServer = FakeIdpServer(oAuthSettings.jwkUri)
    val public = rsaKeyPair.public as RSAPublicKey

    override fun withHttpClient(block: (client: HttpHandler, cookieTokenStore: MutableMap<String, AccessToken>, oAuthSettings: OAuthSettings) -> Unit) {
        fakeIdpServer.primeJwks(
            PrimedJwk(
                kid = keyId,
                alg = "RS256",
                n = Base64.UrlSafe.encode(public.modulus.toByteArray()),
                e = Base64.UrlSafe.encode(public.publicExponent.toByteArray()),
            )
        )

        val cookieTokenStore = mutableMapOf<String, AccessToken>()
        val underTest = OAuthSecureHandler(
            routes = OAuthSecureHandler.Routes(
                apiRoutes = "/api/test" bind Method.GET to { Response(Status.OK) },
                authFilteredRoutes = "/api/other" bind Method.GET to { Response(Status.OK) },
            ),
            oAuthSettings = oAuthSettings,
            oAuthClient = fakeIdpServer,
            clock = Clock.System,
            cookieTokenStore = cookieTokenStore,
        )

        block(underTest, cookieTokenStore, oAuthSettings)
    }

    override fun withBearerAuthHttpClient(block: (client: HttpHandler) -> Unit) {
        val private = rsaKeyPair.private as RSAPrivateKey

        fakeIdpServer.primeJwks(
            PrimedJwk(
                kid = keyId,
                alg = "RS256",
                n = Base64.UrlSafe.encode(public.modulus.toByteArray()),
                e = Base64.UrlSafe.encode(public.publicExponent.toByteArray()),
            )
        )

        val cookieTokenStore = mutableMapOf<String, AccessToken>()
        val underTest = OAuthSecureHandler(
            routes = OAuthSecureHandler.Routes(
                apiRoutes = "/api/test" bind Method.GET to { Response(Status.OK) },
                authFilteredRoutes = "/api/other" bind Method.GET to { Response(Status.OK) },
            ),
            oAuthSettings = oAuthSettings,
            oAuthClient = fakeIdpServer,
            clock = Clock.System,
            cookieTokenStore = cookieTokenStore,
        )

        block(
            ClientFilters.BearerAuth(
                generateSignedJwt(
                    private,
                    keyId,
                    oAuthSettings.issuerUri.toString(),
                    oAuthSettings.audience
                )
            )
                .then(underTest),
        )
    }

    override fun retrieveValidJwt(oAuthSettings: OAuthSettings): String =
        generateSignedJwt(
            rsaKeyPair.private as RSAPrivateKey,
            keyId,
            oAuthSettings.issuerUri.toString(),
            oAuthSettings.audience
        )

    @Test
    fun `api request with bearer token not signed by trusted source should 401`() {
        val anotherPrivateKey = generateRsaKeyPair().private as RSAPrivateKey

        val cookieTokenStore = mutableMapOf<String, AccessToken>()
        val underTest = OAuthSecureHandler(
            routes = OAuthSecureHandler.Routes(
                apiRoutes = "/api/test" bind Method.GET to { Response(Status.OK) },
                authFilteredRoutes = "/api/other" bind Method.GET to { Response(Status.OK) },
            ),
            oAuthSettings = oAuthSettings,
            oAuthClient = fakeIdpServer,
            clock = Clock.System,
            cookieTokenStore = cookieTokenStore,
        )

        val client =
            ClientFilters.BearerAuth(generateSignedJwt(privateKey = anotherPrivateKey, keyId = "some-id"))
                .then(underTest)

        val response = client(
            Request(Method.GET, "/api/test")
        )

        response shouldHaveStatus UNAUTHORIZED
    }

    fun generateSignedJwt(
        privateKey: RSAPrivateKey,
        keyId: String? = null,
        issuer: String? = null,
        audience: String? = null
    ): String {
        val algorithm = Algorithm.RSA256(privateKey)
        val validJwt = JWT.create()
            .apply {
                withSubject("some-subject")
                audience?.let { withAudience(it) }
                issuer?.let { withIssuer(it) }
                keyId?.let { withKeyId(it) }
            }
            .sign(algorithm)
        return validJwt
    }

    fun generateRsaKeyPair(): KeyPair {
        val keyPair = with(KeyPairGenerator.getInstance("RSA")) {
            initialize(4096)
            generateKeyPair()
        }
        return keyPair
    }
}