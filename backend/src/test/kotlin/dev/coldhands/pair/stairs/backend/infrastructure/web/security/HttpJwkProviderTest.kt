package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwk.SigningKeyNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

class HttpJwkProviderTest {
    private val uri = Uri.of("/some/path/to/keys")

    @Nested
    inner class HappyPath {

        @Test
        fun `return jwk when keyId exists in jwks`() {
            val underTest = HttpJwkProvider(uri) {
                Response(Status.OK)
                    .body(
                        """
                            {
                              "keys": [
                                {
                                  "use": "sig",
                                  "kty": "RSA",
                                  "kid": "first",
                                  "alg": "RS256",
                                  "n": "abc",
                                  "e": "def"
                                }
                              ]
                            }
                        """.trimIndent()
                    )

            }

            underTest.get("first") should {
                it.id shouldBe "first"
                it.type shouldBe "RSA"
                it.algorithm shouldBe "RS256"
                it.usage shouldBe "sig"
                it.operationsAsList shouldBe null
                it.certificateUrl shouldBe null
                it.certificateChain shouldBe null
                it.certificateThumbprint shouldBe null
                it.additionalAttributes shouldBe mapOf(
                    "n" to "abc",
                    "e" to "def"
                )
            }
        }

        @Test
        fun `return jwk when keyId is second in jwks`() {
            val underTest = HttpJwkProvider(uri) {
                Response(Status.OK)
                    .body(
                        """
                            {
                              "keys": [
                                {
                                  "use": "sig",
                                  "kty": "RSA",
                                  "kid": "first",
                                  "alg": "RS256",
                                  "n": "abc",
                                  "e": "def"
                                },
                                {
                                  "use": "sig",
                                  "kty": "RSA",
                                  "kid": "second",
                                  "alg": "RS256",
                                  "n": "ghi",
                                  "e": "jkl"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
            }

            underTest.get("second") should {
                it.id shouldBe "second"
                it.type shouldBe "RSA"
                it.algorithm shouldBe "RS256"
                it.usage shouldBe "sig"
                it.additionalAttributes shouldBe mapOf(
                    "n" to "ghi",
                    "e" to "jkl"
                )
            }
        }

        @Test
        fun `get all returns all jwk in jwks`() {
            val underTest = HttpJwkProvider(uri) {
                Response(Status.OK)
                    .body(
                        """
                            {
                              "keys": [
                                {
                                  "use": "sig",
                                  "kty": "RSA",
                                  "kid": "first",
                                  "alg": "RS256",
                                  "n": "abc",
                                  "e": "def"
                                },
                                {
                                  "use": "sig",
                                  "kty": "RSA",
                                  "kid": "second",
                                  "alg": "RS256",
                                  "n": "ghi",
                                  "e": "jkl"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
            }

            underTest.getAll().shouldMatchEach(
                {
                    it.id shouldBe "first"
                    it.additionalAttributes shouldContain ("n" to "abc")
                },
                {
                    it.id shouldBe "second"
                    it.additionalAttributes shouldContain ("n" to "ghi")
                },
            )
        }
    }

    @Nested
    inner class SadPath {

        @Test
        fun `throw exception when keyId is null`() {
            val underTest = HttpJwkProvider(uri) { error("unused") }
            shouldThrow<SigningKeyNotFoundException> { underTest.get(null) }
                .message shouldBe "No key found in $uri with kid ${null}"
        }

        @Test
        fun `throw exception when keyId is not found in jwks`() {
            val underTest = HttpJwkProvider(uri) {
                Response(Status.OK)
                    .body(
                        """
                            {
                              "keys": [
                                {
                                  "use": "sig",
                                  "kty": "RSA",
                                  "kid": "first",
                                  "alg": "RS256",
                                  "n": "abc",
                                  "e": "def"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
            }

            val randomKeyId = UUID.randomUUID().toString()

            shouldThrow<SigningKeyNotFoundException> { underTest.get(randomKeyId) }
                .message shouldBe "No key found in $uri with kid $randomKeyId"
        }

        @Test
        fun `throw exception when exception happens while interacting with server`() {
            val cause = RuntimeException("some-error")
            val underTest = HttpJwkProvider(uri) { throw cause }

            shouldThrow<SigningKeyNotFoundException> { underTest.get("some-key") }.should {
                it.message shouldBe "Cannot obtain jwks from url $uri"
                it.cause shouldBe cause
            }
        }

        @Test
        fun `throw exception when response body is not json`() {
            val underTest = HttpJwkProvider(uri) { Response(Status.OK).body("<xml/>") }

            shouldThrow<SigningKeyNotFoundException> { underTest.get("some-key") }.should {
                it.message shouldBe "Cannot obtain jwks from url $uri"
            }
        }

        @Test
        fun `throw exception when no 'keys' in json`() {
            val underTest = HttpJwkProvider(uri) { Response(Status.OK).body("{}") }

            shouldThrow<SigningKeyNotFoundException> { underTest.get("some-key") }.should {
                it.message shouldBe "No keys found in $uri"
            }
        }

        @Test
        fun `throw exception when 'keys' is not an array`() {
            val underTest = HttpJwkProvider(uri) {
                Response(Status.OK).body(
                    """
                        { "keys": "abc" }
                    """.trimIndent()
                )
            }

            shouldThrow<SigningKeyNotFoundException> { underTest.get("some-key") }.should {
                it.message shouldBe "No keys found in $uri"
            }
        }

        @Test
        fun `throw exception when 'keys' is an empty array`() {
            val underTest = HttpJwkProvider(uri) {
                Response(Status.OK).body(
                    """
                        { "keys": [] }
                    """.trimIndent()
                )
            }

            shouldThrow<SigningKeyNotFoundException> { underTest.get("some-key") }.should {
                it.message shouldBe "No keys found in $uri"
            }
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                """ 1 """,
                """ "string" """,
                """ [] """,
                """ {"abc": {}}, [] """,
            ]
        )
        fun `throw exception when 'keys' is not an list of string maps`(keys: String) {
            val underTest = HttpJwkProvider(uri) {
                Response(Status.OK).body(
                    """
                        { "keys": [$keys] }
                    """.trimIndent()
                )
            }

            shouldThrow<SigningKeyNotFoundException> { underTest.get("some-key") }.should {
                it.message shouldBe "Failed to parse jwk from json"
            }
        }

        @Test
        fun `throw exception when a jwk is invalid`() {
            val httpHandler: HttpHandler = {
                Response(Status.OK)
                    .body(
                        """
                            {
                              "keys": [
                                {
                                  "nonsense": true
                                }
                              ]
                            }
                        """.trimIndent()
                    )
            }

            val underTest = HttpJwkProvider(uri, httpHandler)

            shouldThrow<SigningKeyNotFoundException> { underTest.get("some-key") }.should {
                it.message shouldBe "Failed to parse jwk from json"
            }
        }
    }
}