package dev.coldhands.pair.stairs.backend.domain.stream

import dev.coldhands.pair.stairs.backend.domain.StreamId

data class StreamInfo(
    val id: StreamId,
    val displayName: String,
    val archived: Boolean,
): Comparable<StreamInfo> {
    override fun compareTo(other: StreamInfo): Int {
        return compareBy<StreamInfo> { it.displayName }.compare(this, other)
    }
}
