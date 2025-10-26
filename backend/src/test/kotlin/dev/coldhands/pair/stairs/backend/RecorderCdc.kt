package dev.coldhands.pair.stairs.backend

import org.http4k.core.HttpHandler
import org.junit.jupiter.api.Test

abstract class RecorderCdc {

    abstract val client: HttpHandler

    @Test
    fun `records answer`() {
        Recorder(client).record(123)
        checkAnswerRecorded()
    }

    open fun checkAnswerRecorded() {}

}