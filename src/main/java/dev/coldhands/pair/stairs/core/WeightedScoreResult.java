package dev.coldhands.pair.stairs.core;

public record WeightedScoreResult(ScoreResult delegate, int weight) implements ScoreResult {

    @Override
    public int score() {
        return delegate.score() * weight;
    }
}
