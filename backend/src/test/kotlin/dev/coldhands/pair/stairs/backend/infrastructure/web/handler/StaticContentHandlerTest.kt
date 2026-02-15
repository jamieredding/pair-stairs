package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.infrastructure.web.testContext
import io.kotest.matchers.string.shouldContain
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.Test

class StaticContentHandlerTest {

    @Test
    fun `can serve static content`() = testContext {
        val response = underTest(Request(GET, "/"))

        response shouldHaveStatus OK
        response.bodyString() shouldContain "This is a test index page"
    }

    @Test
    fun `should return index page if requested path does not exist`() = testContext {
        val response = underTest(Request(GET, "/some-path-that-does-not-exist"))

        response shouldHaveStatus OK
        response.bodyString() shouldContain "This is a test index page"
    }
}