package dev.coldhands.pair.stairs.backend.infrastructure.web.security

import dev.coldhands.pair.stairs.backend.infrastructure.web.TestContext
import org.http4k.core.*
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.cookie.cookie
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.accept
import org.http4k.security.AccessToken
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*


interface OAuthSecureHandlerCdc {

    fun withHttpClient(block: (client: HttpHandler, testContext: TestContext) -> Unit)
    fun withBearerAuthHttpClient(block: (client: HttpHandler, testContext: TestContext) -> Unit)
    fun retrieveValidJwt(): String

    @Test
    fun `api request without authorization should 401`() = withHttpClient { client, _ ->
        val response = client(
            Request(Method.GET, "/api/v1/teams")
                .accept(APPLICATION_JSON)
        )

        response shouldHaveStatus UNAUTHORIZED
    }

    @Test
    @Disabled
    fun `unprotected route without authorization should return successfully`() = withHttpClient { client, _ ->
        TODO()
    }

    @Test
    fun `api route with bearer token should return successfully`() = withBearerAuthHttpClient { client, _ ->
        val response = client(
            Request(Method.GET, "/api/v1/teams")
                .accept(APPLICATION_JSON)
        )

        response shouldHaveStatus OK
    }

    @Test
    fun `api route with cookie should return successfully`() = withHttpClient { client, testContext ->
        val cookieValue = UUID.randomUUID().toString()
        val client = Filter { next ->
            { request ->
                next(request.cookie("pair-stairsAuth", cookieValue))
            }
        }
            .then(client)

        testContext.cookieTokenStore[cookieValue] = AccessToken(value = retrieveValidJwt())

        val response = client(
            Request(Method.GET, "/api/v1/teams")
                .accept(APPLICATION_JSON)
        )

        response shouldHaveStatus OK
    }
}
