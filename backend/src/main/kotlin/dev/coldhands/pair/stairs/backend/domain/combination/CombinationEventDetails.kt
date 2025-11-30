package dev.coldhands.pair.stairs.backend.domain.combination

import java.time.LocalDate

data class CombinationEventDetails(
    val date: LocalDate,
    val combination: Set<CombinationEvent.PairStream>
)
