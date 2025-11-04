package dev.coldhands.pair.stairs.backend.domain

data class User(
    val id: UserId,
    val oidcSub: OidcSub,
    val displayName: String
)