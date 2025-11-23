package dev.coldhands.pair.stairs.backend.domain.stream

import dev.coldhands.pair.stairs.backend.domain.StreamId

data class Stream(
    val id: StreamId,
    val name: String,
    val archived: Boolean,
)
