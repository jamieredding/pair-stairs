package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.Matchers.answerShouldBe
import io.kotest.matchers.shouldBe
import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EndToEndTest {

    private val recorder = FakeRecorderHttp()
    private val recorderServer = recorder.asServer(Jetty(0)).apply { start() }
    private val client = OkHttp()
    private val server = MyMathServer(0, Uri.of("http://localhost:${recorderServer.port()}"))

    @BeforeEach
    fun setUp() {
        recorderServer.start()
        server.start()
        recorder.calls.clear()
    }

    @AfterEach
    fun tearDown() {
        server.stop()
        recorderServer.stop()
        recorder.calls.clear()
    }

    @Test
    fun `all endpoints are mounted correctly`() {
        client(Request(GET, "http://localhost:${server.port()}/ping")) shouldHaveStatus OK
        client(Request(GET, "http://localhost:${server.port()}/add?value=1&value=2")) answerShouldBe 3
        client(Request(GET, "http://localhost:${server.port()}/multiply?value=1&value=2")) answerShouldBe 2

        recorder.calls shouldBe listOf(3, 2)
    }
}

object Matchers {
    infix fun Response.answerShouldBe(expected: Int) {
        this.shouldHaveStatus(OK)
        this.shouldHaveBody(expected.toString())
    }
}