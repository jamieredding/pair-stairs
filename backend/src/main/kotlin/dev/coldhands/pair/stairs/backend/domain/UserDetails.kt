package dev.coldhands.pair.stairs.backend.domain

data class UserDetails(
    val oidcSub: OidcSub?, // todo http4k-vertical-slice make these non nullable
    val displayName: String? // todo http4k-vertical-slice make these non nullable
)
