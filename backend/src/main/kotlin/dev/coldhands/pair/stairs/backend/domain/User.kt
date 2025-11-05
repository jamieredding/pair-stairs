package dev.coldhands.pair.stairs.backend.domain

import java.time.Instant

data class User(
    val id: UserId,
    val oidcSub: OidcSub,
    val displayName: String,

    val createdAt: Instant,
    val updatedAt: Instant,
)