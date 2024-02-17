package dev.coldhands.pair.stairs.core.domain;

public record WeightedScoreResult(ScoreResult delegate, int weight) implements ScoreResult {

    @Override
    public int score() {
        return delegate.score() * weight;
    }
}
