package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DeveloperInfoTest {

    @Test
    fun comparable() {
        val a = DeveloperInfo(
            id = DeveloperId(1),
            displayName = "a",
            archived = false
        )
        val b = a.copy(displayName = "b")

        a.compareTo(b) shouldBeLessThan 0
        b.compareTo(a) shouldBeGreaterThan 0
        a.compareTo(a) shouldBe 0
    }
}