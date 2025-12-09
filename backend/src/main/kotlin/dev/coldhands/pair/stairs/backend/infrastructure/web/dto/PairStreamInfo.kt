package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperInfo
import dev.coldhands.pair.stairs.backend.domain.stream.StreamInfo

data class PairStreamInfo(
    val developers: List<DeveloperInfo>,
    val stream: StreamInfo
): Comparable<PairStreamInfo> {
    override fun compareTo(other: PairStreamInfo): Int {
        return compareBy<PairStreamInfo> { it.stream }.compare(this, other)
    }
}