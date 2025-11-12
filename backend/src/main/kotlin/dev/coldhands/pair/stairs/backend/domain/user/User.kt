package dev.coldhands.pair.stairs.backend.domain.user

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
import java.time.Instant

data class User(
    val id: UserId,
    val oidcSub: OidcSub,
    val displayName: String,

    val createdAt: Instant,
    val updatedAt: Instant,
)