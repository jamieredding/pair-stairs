package dev.coldhands.pair.stairs.core.domain;

@FunctionalInterface
public interface ScoringRule<Combination> {  // todo make it take pair not combination

    ScoreResult score(Combination combination);
}
