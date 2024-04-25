package dev.coldhands.pair.stairs.backend.infrastructure.web.dto;


import java.util.List;

public record ScoredCombinationDto(int score, List<PairStreamDto> combinations) {
    // todo should this be a ScoredCombination?
}
