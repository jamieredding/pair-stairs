package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import java.time.Instant

class FakeDateProvider: DateProvider {

    var now: Instant = Instant.now()

    override fun instant(): Instant = now
}