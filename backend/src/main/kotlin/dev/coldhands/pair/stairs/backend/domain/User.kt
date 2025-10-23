package dev.coldhands.pair.stairs.backend.domain

data class User(
    val id: Long,
    val oidcSub: String?,
    val displayName: String?
)