package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.StreamId

data class CalculateInputDto(
    val developerIds: List<DeveloperId>,
    val streamIds: List<StreamId>
)
