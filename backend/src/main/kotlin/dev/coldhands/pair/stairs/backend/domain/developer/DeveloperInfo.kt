package dev.coldhands.pair.stairs.backend.domain.developer

import dev.coldhands.pair.stairs.backend.domain.DeveloperId

// todo should this be called a Dto?
data class DeveloperInfo(
    val id: DeveloperId,
    val displayName: String,
    val archived: Boolean,
): Comparable<DeveloperInfo> {
    override fun compareTo(other: DeveloperInfo): Int {
        return compareBy<DeveloperInfo> { it.displayName }.compare(this, other)
    }

}
