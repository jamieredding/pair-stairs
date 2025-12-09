package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

data class ScoredCombinationInfo(
    val score: Int,
    val combination: List<PairStreamInfo>
)
