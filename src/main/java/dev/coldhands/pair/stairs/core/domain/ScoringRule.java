package dev.coldhands.pair.stairs.core.domain;

@FunctionalInterface
public interface ScoringRule<Combination> {

    ScoreResult score(Combination combination);
}
