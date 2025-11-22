package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.domain.stream.RelatedStreamStats

data class DeveloperStats(
    val developerStats: List<RelatedDeveloperStats>,
    val streamStats: List<RelatedStreamStats>
)
