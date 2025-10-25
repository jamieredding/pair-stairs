package dev.coldhands.pair.stairs.backend

import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
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
    fun `responds to ping`() {
        val response = client(Request(GET, "http://localhost:${server.port()}/ping"))
        response shouldHaveStatus OK
    }
}