package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.coldhands.pair.stairs.backend.infrastructure.web.TestContext
import dev.coldhands.pair.stairs.backend.infrastructure.web.testContext
import org.http4k.config.Environment
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.accept
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*
import kotlin.io.encoding.Base64

class OAuthSecureHandlerTest : OAuthSecureHandlerCdc {
    private val rsaKeyPair = generateRsaKeyPair()
    private val keyId = UUID.randomUUID().toString()

    override fun withHttpClient(block: (client: HttpHandler, testContext: TestContext) -> Unit) {
        testContext {
            val public = rsaKeyPair.public as RSAPublicKey

            // todo you shouldn't need to cast this here
            (oauthClient as FakeIdpServer).primeJwks(
                PrimedJwk(
                    kid = keyId,
                    alg = "RS256",
                    n = Base64.UrlSafe.encode(public.modulus.toByteArray()),
                    e = Base64.UrlSafe.encode(public.publicExponent.toByteArray()),
                )
            )

            environment = Environment.from(
                "oauth.jwk.uri" to "/some/path", // todo use a proper path here
                "oauth.issuer.uri" to "/another/path", // todo use a proper path here
                "oauth.audience" to "pair-stairs", // todo use a proper path here
            ).overrides(environment)

            block(underTest, this)
        }
    }

    override fun withBearerAuthHttpClient(block: (client: HttpHandler, testContext: TestContext) -> Unit) {
        testContext {
            val public = rsaKeyPair.public as RSAPublicKey
            val private = rsaKeyPair.private as RSAPrivateKey

            // todo you shouldn't need to cast this here
            (oauthClient as FakeIdpServer).primeJwks(
                PrimedJwk(
                    kid = keyId,
                    alg = "RS256",
                    n = Base64.UrlSafe.encode(public.modulus.toByteArray()),
                    e = Base64.UrlSafe.encode(public.publicExponent.toByteArray()),
                )
            )

            environment = Environment.from(
                "oauth.jwk.uri" to "/some/path", // todo use a proper path here
                "oauth.issuer.uri" to "/another/path", // todo use a proper path here
                "oauth.audience" to "pair-stairs", // todo use a proper path here
            ).overrides(environment)

            // todo configure jwks lookup to return public part of the generated private key
            block(
                ClientFilters.BearerAuth(generateSignedJwt(private, keyId, "/another/path", "pair-stairs"))
                    .then(underTest),
                this
            )
        }
    }

    override fun retrieveValidJwt(): String =
        generateSignedJwt(rsaKeyPair.private as RSAPrivateKey, keyId, "/another/path", "pair-stairs")

    @Test
    fun `api request with bearer token not signed by trusted source should 401`() {
        testContext {
            val public = rsaKeyPair.public as RSAPublicKey

            val anotherPrivateKey = generateRsaKeyPair().private as RSAPrivateKey

            // todo you shouldn't need to cast this here
            (oauthClient as FakeIdpServer).primeJwks(
                PrimedJwk(
                    kid = keyId,
                    alg = "RS256",
                    n = Base64.UrlSafe.encode(public.modulus.toByteArray()),
                    e = Base64.UrlSafe.encode(public.publicExponent.toByteArray()),
                )
            )

            environment = Environment.from(
                "oauth.jwk.uri" to "/some/path" // todo use a proper path here
            ).overrides(environment)

            val client =
                ClientFilters.BearerAuth(generateSignedJwt(privateKey = anotherPrivateKey, keyId = "some-id"))
                    .then(underTest)

            val response = client(
                Request(Method.GET, "/api/v1/teams")
                    .accept(APPLICATION_JSON)
            )

            response shouldHaveStatus UNAUTHORIZED
        }
    }

    fun generateSignedJwt(privateKey: RSAPrivateKey, keyId: String? = null, issuer: String? = null, audience: String? = null): String {
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