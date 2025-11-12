package dev.coldhands.pair.stairs.backend.domain.user

import dev.coldhands.pair.stairs.backend.domain.OidcSub

data class UserDetails(
    val oidcSub: OidcSub,
    val displayName: String
)
