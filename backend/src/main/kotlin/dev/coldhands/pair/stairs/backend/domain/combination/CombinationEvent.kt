package dev.coldhands.pair.stairs.backend.domain.combination

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId
import java.time.LocalDate

data class CombinationEvent(
    val id: CombinationEventId,
    val date: LocalDate,
    val combination: Set<PairStream>
) {
    data class PairStream(
        val developerIds: Set<DeveloperId>,
        val streamId: StreamId
    )
}
