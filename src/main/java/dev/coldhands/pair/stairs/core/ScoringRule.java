package dev.coldhands.pair.stairs.core;

@FunctionalInterface
public interface ScoringRule<Combination> {

    ScoreResult score(Combination combination);
}
