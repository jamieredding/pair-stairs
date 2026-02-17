package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import org.http4k.core.Uri
import java.net.URI
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

fun jwtVerifier(jwkUri: Uri, issuer: Uri, audience: String): JWTVerifier {
    val jwkProvider = JwkProviderBuilder(URI.create(jwkUri.toString()).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val rsaKeyProvider = object : RSAKeyProvider {
        override fun getPublicKeyById(keyId: String) = jwkProvider.get(keyId).publicKey as RSAPublicKey

        override fun getPrivateKey() = null

        override fun getPrivateKeyId() = null
    }

    return JWT.require(Algorithm.RSA256(rsaKeyProvider))
        .withIssuer(issuer.toString())
        .withAudience(audience)
        .build()
}