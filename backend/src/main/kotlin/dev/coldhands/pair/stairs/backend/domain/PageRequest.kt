package dev.coldhands.pair.stairs.backend.domain

data class PageRequest<T>(
    val requestedPage: Int,
    val pageSize: Int,
    val sort: T
)
