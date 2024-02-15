package dev.coldhands.pair.stairs.core;

@FunctionalInterface
public interface ScoringRule<Combination, ScoredCombination> {

    ScoredCombination score(Combination combination);
}
