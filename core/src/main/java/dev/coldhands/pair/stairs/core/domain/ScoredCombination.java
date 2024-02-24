package dev.coldhands.pair.stairs.core.domain;

import java.util.List;

public record ScoredCombination<T>(Combination<T> combination, int totalScore, List<ScoreResult> scoreResults) implements Comparable<ScoredCombination<?>> {

    @Override
    public int compareTo(ScoredCombination<?> o) {
        return Integer.compare(this.totalScore, o.totalScore);
    }
}
