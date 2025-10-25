package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.Matchers.answerShouldBe
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.Test

class AddFunctionalTest {

    private val client = MyMathApp()

    @Test
    fun `adds values together`() {
        client(Request(GET, "/add?value=1&value=2")) answerShouldBe 3
    }

    @Test
    fun `answer is zero when no values`() {
        client(Request(GET, "/add")) answerShouldBe 0
    }

    @Test
    fun `bad request when some values are not numbers`() {
        client(Request(GET, "/add?value=1&value=notANumber")) shouldHaveStatus BAD_REQUEST
    }
}