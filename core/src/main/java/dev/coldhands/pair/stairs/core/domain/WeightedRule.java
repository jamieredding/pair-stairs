package dev.coldhands.pair.stairs.core.domain;

public class WeightedRule<T> implements ScoringRule<T> {
    private final int weight;
    private final ScoringRule<T> delegate;

    public WeightedRule(int weight, ScoringRule<T> delegate) {
        this.weight = weight;
        this.delegate = delegate;
    }

    @Override
    public ScoreResult score(Combination<T> combination) {
        return new WeightedScoreResult(delegate.score(combination), weight);
    }
}
