package dev.coldhands.pair.stairs.backend.domain;


import java.util.List;

public record ScoredAssignment(int score, List<PairStream> combinations) {
    // todo should this be a ScoredCombination?
}
