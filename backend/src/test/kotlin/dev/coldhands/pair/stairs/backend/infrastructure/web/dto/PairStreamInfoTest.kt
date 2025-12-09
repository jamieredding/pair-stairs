package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.stream.StreamInfo
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PairStreamInfoTest {

    @Test
    fun comparable() {
        val a = PairStreamInfo(
            developers = emptyList(),
            stream = StreamInfo(
                id = StreamId(1),
                displayName = "a",
                archived = false
            )
        )
        val b = a.copy(stream = a.stream.copy(displayName = "b"))

        a shouldBeLessThan b
        b shouldBeGreaterThan a
        a.compareTo(a) shouldBe 0
    }
}