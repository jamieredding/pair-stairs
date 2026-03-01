package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import java.security.interfaces.RSAPublicKey

// todo test, expired, wrong audience, wrong issuer, etc
fun jwtVerifier(jwkUri: Uri, issuer: Uri, audience: String, client: HttpHandler): JWTVerifier {
    // todo, use caching and ratelimiting
    val jwkProvider = HttpJwkProvider(jwkUri, client)
//    val cachedAndRateLimitedJwkProvider = GuavaCachedJwkProvider(
//        RateLimitedJwkProvider(
//            jwkProvider,
//            BucketImpl(10, 1, TimeUnit.MINUTES)
//        ),
//        10,
//        24, TimeUnit.HOURS
//    )

    val rsaKeyProvider = object : RSAKeyProvider {
        // todo test this function
        override fun getPublicKeyById(keyId: String) = resultFrom { jwkProvider.get(keyId).publicKey }
            .flatMap { resultFrom { it as RSAPublicKey } }
            .mapFailure { exception -> throw IllegalArgumentException("Key with id $keyId is not supported", exception) }
            .get()

        override fun getPrivateKey() = null

        override fun getPrivateKeyId() = null
    }

    return JWT.require(Algorithm.RSA256(rsaKeyProvider))
        .withIssuer(issuer.toString())
        .withAudience(audience)
        .build()
}