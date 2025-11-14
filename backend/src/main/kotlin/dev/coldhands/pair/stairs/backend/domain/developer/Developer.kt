package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.domain.DeveloperId

data class Developer(
    val id: DeveloperId,
    val name: String,
    val archived: Boolean,
)
