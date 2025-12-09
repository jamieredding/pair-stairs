package dev.coldhands.pair.stairs.backend.domain.stream

import dev.coldhands.pair.stairs.backend.domain.StreamId

// todo should this be called a Dto?
data class StreamInfo(
    val id: StreamId,
    val displayName: String,
    val archived: Boolean,
): Comparable<StreamInfo> {
    override fun compareTo(other: StreamInfo): Int {
        return compareBy<StreamInfo> { it.displayName }.compare(this, other)
    }
}
