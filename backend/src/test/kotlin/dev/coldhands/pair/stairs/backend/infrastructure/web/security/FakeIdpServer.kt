package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes

class FakeIdpServer(jwksUri: Uri) : HttpHandler {

    private var primedResponses: ArrayDeque<HttpHandler> = ArrayDeque()
    private val jwksLens = Body.auto<Jwks>().toLens()

    private val app = routes(
        jwksUri.path bind Method.GET to {
            primedResponses.removeFirst()(it)
        }
    )

    override fun invoke(request: Request) = app(request)

    fun primeJwks(vararg jwksToPrime: PrimedJwk) {
        primedResponses.addLast {
            jwksLens(
                Jwks(jwksToPrime.toList()),
                Response.Companion(Status.OK)
            )
        }
    }

    fun primeJwks(httpHandler: HttpHandler) {
        primedResponses.addLast(httpHandler)
    }

}

@Suppress("unused")
class Jwks(
    val keys: List<PrimedJwk>
)

@Suppress("unused")
class PrimedJwk(
    val use: String = "sig",
    val kty: String = "RSA",
    val kid: String = "first",
    val alg: String = "RS256",
    val n: String = "abc",
    val e: String = "def"
)