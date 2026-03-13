package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class FakeDateProvider : DateProvider {

    var now: Instant = Instant.now()

    fun set(string: String) {
        now = resultFrom {
            LocalDate.parse(string)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
        }
            .mapFailure { Instant.parse(string) }
            .get()
    }

    override fun instant(): Instant = now
}