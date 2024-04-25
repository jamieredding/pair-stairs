package dev.coldhands.pair.stairs.backend.domain;


import java.util.List;

public record ScoredCombination(int score, List<PairStream> combination) {
}
