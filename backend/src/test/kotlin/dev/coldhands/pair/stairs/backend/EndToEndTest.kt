package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.Matchers.answerShouldBe
import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EndToEndTest {

    private val client = OkHttp()
    private val server = MyMathServer(0)

    @BeforeEach
    fun setUp() {
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop()
    }

    @Test
    fun `all endpoints are mounted correctly`() {
        client(Request(GET, "http://localhost:${server.port()}/ping")) shouldHaveStatus OK
        client(Request(GET, "http://localhost:${server.port()}/add?value=1&value=2")) answerShouldBe 3
    }
}

object Matchers {
    infix fun Response.answerShouldBe(expected: Int) {
        this.shouldHaveStatus(OK)
        this.shouldHaveBody(expected.toString())
    }
}