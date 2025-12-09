package dev.coldhands.pair.stairs.backend.domain.combination

data class ScoredCombination(
    val score: Int,
    val combination: Set<PairStream>
)
