package dev.coldhands.pair.stairs.backend.domain.stream

import dev.coldhands.pair.stairs.backend.domain.developer.RelatedDeveloperStats

data class StreamStats(
    val developerStats: List<RelatedDeveloperStats>,
)
