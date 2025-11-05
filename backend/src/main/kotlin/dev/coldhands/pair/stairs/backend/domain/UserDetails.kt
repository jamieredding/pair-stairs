package dev.coldhands.pair.stairs.backend.domain

data class UserDetails(
    val oidcSub: OidcSub,
    val displayName: String
)
