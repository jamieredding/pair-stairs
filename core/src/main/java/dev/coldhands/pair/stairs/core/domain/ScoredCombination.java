package dev.coldhands.pair.stairs.core.domain;

import java.util.Comparator;
import java.util.List;

public record ScoredCombination<T>(Combination<T> combination, int totalScore, List<ScoreResult> scoreResults) implements Comparable<ScoredCombination<T>> {

    @Override
    public int compareTo(ScoredCombination<T> o) {
        return Comparator.<ScoredCombination<T>>comparingInt(c -> c.totalScore)
                .thenComparingInt(c -> c.combination.hashCode())
                .compare(this, o);
    }
}
