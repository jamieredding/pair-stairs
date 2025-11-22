package dev.coldhands.pair.stairs.backend.domain.stream

import dev.coldhands.pair.stairs.backend.domain.StreamInfo

data class RelatedStreamStats(
    val stream: StreamInfo,
    val count: Long
)
