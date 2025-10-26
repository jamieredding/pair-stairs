package dev.coldhands.pair.stairs.backend

import io.kotest.matchers.shouldBe
import org.http4k.core.HttpHandler
import org.http4k.core.Request

class FakeRecorderTest : RecorderCdc() {

    val underTest = FakeRecorderHttp()

    override val client: HttpHandler = { request: Request -> underTest(request) }

    override fun checkAnswerRecorded() {
        underTest.calls shouldBe listOf(123)
    }
}