package dev.coldhands.pair.stairs.backend.domain

import java.time.Instant

class RealDateProvider: DateProvider {
    override fun instant(): Instant = Instant.now()
}