package dev.coldhands.pair.stairs.backend.domain.combination

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId

data class PairStream(
    val developerIds: Set<DeveloperId>,
    val streamId: StreamId
)