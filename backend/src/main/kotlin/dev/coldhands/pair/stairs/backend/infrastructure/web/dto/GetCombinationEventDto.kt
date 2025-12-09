package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import dev.coldhands.pair.stairs.backend.domain.CombinationEventId
import java.time.LocalDate

data class GetCombinationEventDto(
    val id: CombinationEventId,
    val date: LocalDate,
    val combination: List<PairStreamInfo>
)
