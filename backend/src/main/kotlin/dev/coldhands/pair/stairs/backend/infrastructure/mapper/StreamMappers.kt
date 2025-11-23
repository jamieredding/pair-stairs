package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.StreamId
import dev.coldhands.pair.stairs.backend.domain.StreamInfo
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity


fun StreamEntity.toDomain(): Stream =
    Stream(
        id = StreamId(id ?: error("StreamEntity has no id, likely it hasn't been persisted yet")),
        name = name,
        archived = archived
    )

fun Stream.toEntity(): StreamEntity =
    StreamEntity(
        id = id.value,
        name = name,
        archived = archived
    )

fun Stream.toInfo(): StreamInfo = StreamInfo(
    id.value,
    name,
    archived
)