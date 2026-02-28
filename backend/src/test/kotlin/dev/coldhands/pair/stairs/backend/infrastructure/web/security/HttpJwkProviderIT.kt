package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.fasterxml.jackson.annotation.JsonProperty
import dev.coldhands.pair.stairs.backend.InlineArgumentsProvider
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.format.Jackson
import org.http4k.lens.accept
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class HttpJwkProviderIT {

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(IdentityProviderArgumentsProvider::class)
    fun `can get all keys and find key in that key set`(
        @Suppress("unused") idpName: String,
        jwksUri: Uri
    ) {
        val underTest = HttpJwkProvider(jwksUri, JavaHttpClient())

        val firstKey = underTest.getAll().shouldNotBeEmpty().first()

        underTest.get(firstKey.id).should {
            it.id shouldBe firstKey.id
            it.type shouldBe firstKey.type
            it.additionalAttributes shouldBe firstKey.additionalAttributes
        }
    }

}

class IdentityProviderArgumentsProvider : InlineArgumentsProvider({
    Stream.of(
        Arguments.of("local dex", Uri.of("http://localhost:5556/dex/keys")),
        Arguments.of("azure", discoverAzureJwksUri())
    )
})

private fun discoverAzureJwksUri(): Uri {
    val client = JavaHttpClient()
    val jwksUriLens = Jackson.autoBody<OpenidConfiguration>().map { it.jwksUri }.toLens()
    val tenantId = "ef7f5d6e-9b55-4d8e-a0df-b291e3d855f8"

    return client(
        Request(
            Method.GET,
            "https://login.microsoftonline.com/$tenantId/v2.0/.well-known/openid-configuration"
        ).accept(ContentType.APPLICATION_JSON)
    )
        .takeIf { it.status == Status.OK }
        ?.let { response ->
            resultFrom { jwksUriLens(response) }
                .map { Uri.of(it) }
                .mapFailure { throw IllegalStateException("Could not discover Azure JWKs", it) }
                .get()
        }
        ?: error("Could not discover Azure JWKs")
}

class OpenidConfiguration(
    @JsonProperty("jwks_uri")
    val jwksUri: String,
)
