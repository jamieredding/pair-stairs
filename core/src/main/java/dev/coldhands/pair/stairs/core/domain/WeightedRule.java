package dev.coldhands.pair.stairs.core.domain;

public class WeightedRule<Combination> implements ScoringRule<Combination> { // todo make it take pair not combination
    private final int weight;
    private final ScoringRule<Combination> delegate;

    public WeightedRule(int weight, ScoringRule<Combination> delegate) {
        this.weight = weight;
        this.delegate = delegate;
    }

    @Override
    public ScoreResult score(Combination combination) {
        return new WeightedScoreResult(delegate.score(combination), weight);
    }
}
