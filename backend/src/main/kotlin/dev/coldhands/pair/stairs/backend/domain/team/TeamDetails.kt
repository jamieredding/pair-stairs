package dev.coldhands.pair.stairs.backend.domain.team

import dev.coldhands.pair.stairs.backend.domain.Slug

data class TeamDetails(
    val name: String,
    val slug: Slug,
)
