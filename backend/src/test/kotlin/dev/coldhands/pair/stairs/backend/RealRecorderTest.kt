package dev.coldhands.pair.stairs.backend

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.matchers.collections.shouldNotBeEmpty
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.filter.ClientFilters.SetHostFrom
import java.util.*

class RealRecorderTest : RecorderCdc() {
    val testId = UUID.randomUUID().toString()

    val testIdFilter: Filter = Filter { next: HttpHandler ->
        { request: Request ->
            next(request.header("x-test-id", testId))
        }
    }

    override val client: HttpHandler = SetHostFrom(Uri.of("http://localhost:18082"))
        .then(testIdFilter)
        .then(OkHttp())

    override fun checkAnswerRecorded() {
        val wireMock = WireMock("http", "localhost", 18082)

        val matchingRequests = wireMock.find(
            postRequestedFor(urlEqualTo("/123"))
                .withHeader("x-test-id", equalTo(testId))
        )

        matchingRequests.shouldNotBeEmpty()

    }
}