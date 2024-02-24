package dev.coldhands.pair.stairs.core.domain;

@FunctionalInterface
public interface ScoringRule<T> {

    ScoreResult score(Combination<T> combination);
}
