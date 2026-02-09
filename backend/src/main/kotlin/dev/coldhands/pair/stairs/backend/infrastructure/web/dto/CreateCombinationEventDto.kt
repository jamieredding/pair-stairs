package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import dev.coldhands.pair.stairs.backend.domain.combination.PairStream
import java.time.LocalDate

data class CreateCombinationEventDto(
    val date: LocalDate,
    val combination: List<PairStream>
)
