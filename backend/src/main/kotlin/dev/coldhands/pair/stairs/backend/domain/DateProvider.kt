package dev.coldhands.pair.stairs.backend.domain

import java.time.Instant

interface DateProvider {

    fun instant(): Instant
}