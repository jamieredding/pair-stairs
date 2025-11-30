package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.combination.CombinationEvent
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity

fun CombinationEventEntity.toDomain(): CombinationEvent = CombinationEvent(
    id = CombinationEventId(id ?: error("CombinationEventEntity has no id, likely it hasn't been persisted yet")),
    date = date,
    combination = combination.toDomain()
)

fun CombinationEntity.toDomain(): Set<CombinationEvent.PairStream> = pairs
    .map { it.toDomain() }
    .toSet()

fun PairStreamEntity.toDomain() = CombinationEvent.PairStream(
    developerIds = developers.map {
        DeveloperId(
            it.id ?: error("Developer has no id, likely it hasn't been persisted yet")
        )
    }.toSet(),
    streamId = StreamId(stream.id ?: error("Stream has no id, likely it hasn't been persisted yet"))
)