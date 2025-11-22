package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo

data class RelatedDeveloperStats(
    val developer: DeveloperInfo,
    val count: Long,
)
