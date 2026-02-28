package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.format.Jackson

class HttpJwkProvider(
    private val jwkUri: Uri,
    private val client: HttpHandler
) : JwkProvider {

    override fun get(keyId: String?): Jwk {
        return keyId?.let { keyId ->
            getAll()
                .firstOrNull { it.id == keyId }
        }
            ?: throw SigningKeyNotFoundException("No key found in $jwkUri with kid $keyId", null)
    }

    fun getAll(): List<Jwk> {
        val jsonNode: Map<String, Any> = resultFrom { client(Request(GET, jwkUri)) }
            .flatMap { resultFrom { Jackson.asA<Map<String, Any>>(it.bodyString()) } }
            .mapFailure { throw SigningKeyNotFoundException("Cannot obtain jwks from url $jwkUri", it) }
            .get()

        val keys = jsonNode["keys"]
            ?.let { it as? List<*> }
            ?.takeIf { it.isNotEmpty() }
            ?: throw SigningKeyNotFoundException("No keys found in $jwkUri", null)

        return keys.map { key ->
            resultFrom {
                @Suppress("UNCHECKED_CAST")
                Jwk.fromValues(key as MutableMap<String, Any>)
            }
                .mapFailure { throw SigningKeyNotFoundException("Failed to parse jwk from json", it) }
                .get()
        }
    }
}